;; (printf "Current-ns [%s] loading %s ...\n" *ns* 'corona.common)

(ns corona.common
  (:require clj-http.client
            [clj-time.coerce :as ctc]
            [clj-time.core :as ctime]
            [clj-time.format :as ctf]
            [clojure.data.json :as json]
            [clojure.spec.alpha :as spec]
            [clojure.string :as cstr]
            [corona.envdef :as envdef]
            [corona.pom-version-get :as pom]
            [environ.core :as env]
            [taoensso.timbre :as timbre :refer [debugf infof]]
            [utils.core :as utc]
            [clj-memory-meter.core :as meter]
            [utils.num :as utn])
  (:import java.security.MessageDigest))

;; (set! *warn-on-reflection* true)

(def ^:const ^String undef "<UNDEF>")

(spec/def ::port number?)

(def ^:const ^Long webapp-port (if-let [env-port (env/env :port)]
                                 (read-string env-port)
                                 ;; keep port-nr in sync with README.md
                                 5050))

(when-not (spec/valid? ::port webapp-port)
  (throw (Exception.
          (spec/explain-str ::port webapp-port))))

(def environment envdef/environment)

(spec/def ::env-type (set (keys environment)))

(def env-type
  "When deving check:
      echo $CORONA_ENV_TYPE
  When testing locally via `heroku local --env=.heroku-local.env` check
  the file .heroku-local.env

  TODO env-type priority could / should be:
  1. command line parameter
  2. some config/env file - however not the .heroku-local.env
  3. environment variable
  "
  ((comp keyword cstr/lower-case :corona-env-type) env/env))

(when-not (spec/valid? ::env-type env-type)
  (throw (Exception.
          (spec/explain-str ::env-type env-type))))

(spec/def ::fun clojure.core/fn?)

(def ^:const ^String bot-name        (get-in environment
                                             [env-type :bot-name]))
(def ^:const ^String webapp-server   (get-in environment
                                             [env-type :web-server]))
(def ^:const ^String json-api-server (get-in environment
                                             [env-type :json-server]))
(def ^:const ^String graphs-path "graphs")

;; forward declarations
(declare env-corona-cases? env-hokuspokus? env-local? env-devel?)

(defn- define-env-predicates
  "Defines vars:
  `env-env-corona-cases?`, `env-hokuspokus?`, `env-local?`, `env-devel?`"
  []
  (run! (fn [v]
          (def v v)
          (let [symb-v ((comp symbol
                              (partial format "env-%s?")
                              cstr/lower-case
                              name)
                        v)]
            (reset-meta! (intern *ns* symb-v (= env-type v))
                         {:const true :tag `Boolean})))
        (keys environment)))

(define-env-predicates)

(def ^:const ^Boolean env-prod? env-corona-cases?)
(def ^:const ^Boolean on-heroku? (or env-prod? env-hokuspokus?))

(def use-webhook?
  "TODO consider having environment variable (env/env :use-webhook)"
  on-heroku?)

(def ^:const ^String telegram-token (env/env :telegram-token))
(def ^:const ^String repl-user      (env/env :repl-user))
(def ^:const ^String repl-password  (env/env :repl-password))

(defn system-exit [exit-status]
  (debugf "Exiting with status %s ..." exit-status)
  (System/exit exit-status))

(def ^:const chat-id
  "Telegram chat-id."
  "112885364")

(defn calculate-activ [confirmed recovered deaths]
  ((comp
    #_(fn [result]
      (printf "[calculate-activ] (- %s (+ %s %s): %s\n" confirmed recovered deaths result)
      result)
    (fn [confirmed recovered deaths] (- confirmed (+ recovered deaths))))
   confirmed recovered deaths))

(defn calculate-recov [confirmed deaths]
  ((comp
    #_(fn [result]
      (printf "[calculate-recov] (+ %s %s): %s\n" confirmed deaths result)
      result)
    (fn [confirmed deaths] (- confirmed deaths)))
   confirmed deaths))

(defn calc-rate [case-kw]
  (fn [{:keys [v p c r d a] :as prm}]
    (let [ret (case case-kw
                :v
                #_(utn/percentage v p)
                ;; TODO see create-detailed-info
                (utn/round-precision (/ (* v 100.0) p) 1)

                (utn/percentage
                 (case case-kw
                   :a a
                   :r r
                   :d d
                   :c (+ d r))
                 c))]
      #_(when (utc/in? [:a :v] case-kw)
        (debugf "[%s] case-kw %s; ret %s; %s" "calc-rate" case-kw ret (dissoc prm :t)))
      ret)))

(defn per-1e5
  "See https://groups.google.com/forum/#!topic/clojure/nH-E5uD8CY4"
  ([place total-count] (per-1e5 :normal place total-count))
  ([mode place total-count]
   (utn/round mode (/ (* place 1e5) total-count))))

(defn calculate-cases-per-100k [case-kw]
  (fn [{:keys [v p c r d]}]
    (if (zero? p)
      0
      (per-1e5 (case case-kw
                 :v v
                 :a (calculate-activ c r d)
                 :r r
                 :d d
                 :c c)
               p))))

(def botver
  (if-let [commit (env/env :commit)]
    (when (and pom/pom-version commit)
      (format "%s-%s" pom/pom-version commit))
    ;; if-let ... else
    undef))

(defn show-env
  "TODO should the spec checking be done here?"
  []
  (mapv (fn [env-var-q]
          (format "%s: %s"
                  env-var-q
                  (let [env-var (eval env-var-q)]
                    (if (or env-var (false? env-var))
                      (if (utc/in? ['corona.common/telegram-token
                                    'corona.common/repl-password] env-var-q)
                        "<PRESENT>" env-var)
                      ;; if-let ... else
                      undef))))
        ['corona.common/env-type
         'corona.common/use-webhook?
         'corona.common/on-heroku?
         'corona.common/telegram-token
         'corona.common/repl-user
         'corona.common/repl-password
         'corona.common/webapp-server
         'corona.common/webapp-port
         'corona.common/bot-name
         'corona.common/botver]))

;; TODO (System/exit <val>) if some var is undefined

(defn fix-octal-val
  "(read-string \"08\") produces a NumberFormatException - octal numbers
  https://clojuredocs.org/clojure.core/read-string#example-5ccee021e4b0ca44402ef71a"
  [s]
  (cstr/replace s #"^0+" ""))

(defn read-number [v]
  (if (or (empty? v) (= "0" v))
    0
    ((comp read-string fix-octal-val) v)))

(defn left-pad
  ([s padding-len] (left-pad s "0" padding-len))
  ([s with padding-len]
   (cstr/replace (format (str "%" padding-len "s") s) " " with)))

#_
(defn left-pad [s padding-len]
  (str (cstr/join (repeat (- padding-len (count s)) " "))
       s))

(defn right-pad
  ([s padding-len] (right-pad s " " padding-len))
  ([s with padding-len]
   (str s
        (cstr/join (repeat (- padding-len (count s)) with)))))

(defn hash-fn [data]
  (let [hash-size 6
        algorith "md5" ;; "sha1" "sha-256" "sha-512"
        raw (.digest (MessageDigest/getInstance algorith)
                     (.getBytes (str data)))]
    (-> (format "%x" (BigInteger. 1 raw))
        (subs 0 hash-size))))

(defn measure
  "require [corona.common :as com]"
  [obj]
  (meter/measure obj :bytes true))

(defn get-json [url]
  (infof "Requesting json-data from %s ..." url)
  (let [tbeg (System/currentTimeMillis)]
    (let [result (-> url
                     (clj-http.client/get {:accept :json})
                     :body
                     (json/read-str :key-fn clojure.core/keyword))]
      ;; heroku cycling https://devcenter.heroku.com/articles/dynos#restarting
      ;; TODO sanitize against:
      ;; 1. http status 503 - service not available
      ;; Requesting json-data from http://covid-tracker-us.herokuapp.com/all ...
      ;; Nov 17 18:04:52 corona-cases-bot heroku/web.1 Process running mem=615M(120.2%)
      ;; Nov 17 18:04:57 corona-cases-bot app/web.1 Execution error (ExceptionInfo) at slingshot.support/stack-trace (support.clj:201).
      ;; Nov 17 18:04:57 corona-cases-bot app/web.1 clj-http: status 503
      ;;
      ;; 2.
      ;; Requesting json-data from http://coronavirus-tracker-api.herokuapp.com/all ...
      ;; Execution error (ConnectionClosedException) at org.apache.http.impl.io.ContentLengthInputStream/read (ContentLengthInputStream.java:178).
      ;; Premature end of Content-Length delimited message body (expected: 840,718; received: 515,312)
      (infof "Requesting json-data from %s ... %s bytes received in %s ms"
             url
             (measure result)
             (- (System/currentTimeMillis) tbeg))
      result)))

(defn encode-cmd [s] (str (if (empty? s) "" "/") s))

(def ^:const ^String html "HTML")
(def ^:const ^String markdown "Markdown")

(defn encode-pseudo-cmd
  "For displaying e.g. /<command-name>"
  [lexical-token parse_mode]
  {:pre (utc/in? [html markdown] parse_mode)}
  (let [fun (if (= parse_mode html)
              (comp #(cstr/replace % "<" "&lt;")
                    #(cstr/replace % ">" "&gt;"))
              identity)]
    (fun lexical-token)))

(defmacro tore
  "->>-or-eduction. In fact both have the same performance.
  See also https://github.com/rplevy/swiss-arrows"
  [coll & fns]
  `(->> ~coll ~@fns)
  #_`(sequence (eduction ~@fns ~coll)))

(def case-params
  ":idx - defines an order in appearance
  :v ~ vaccinated
  :p ~ population
  :c ~ closed cased
  :r ~ recovered cased
  :d ~ deaths
  :a ~ active cases i.e. ill"
  [{:idx  0 :kw :v                :threshold {:inc (int 1e6) :val (int 1e7)}}
   {:idx  1 :kw :p                :threshold {:inc (int 1e6) :val (int 1e7)}}
   {:idx  2 :kw :c                :threshold {:inc 50000     :val (int 2160e3)}}
   {:idx  3 :kw :r :listing-idx 1 :threshold {:inc 10000     :val (int 1297e3)}}
   {:idx  4 :kw :d :listing-idx 2 :threshold {:inc 1000      :val (int 56e3)}}
   {:idx  5 :kw :a :listing-idx 0 :threshold {:inc 10000     :val (int 669e3)}}
   ;; TODO the order matters: it must be the same as in the info-message
   {:idx  6 :kw :v100k}
   {:idx  7 :kw :a100k}
   {:idx  8 :kw :r100k}
   {:idx  9 :kw :d100k}
   {:idx 10 :kw :c100k}

   {:idx 11 :kw :v-rate}
   {:idx 12 :kw :a-rate}
   {:idx 13 :kw :r-rate}
   {:idx 14 :kw :d-rate}
   {:idx 15 :kw :c-rate} ;; closed-rate
   ])

(def aggregation-params
  ":idx - defines an order in appearance"
  [
   {:idx  0 :kw :sum}
   {:idx  1 :kw :abs}
   ])

(def aggregation-cases
  (tore aggregation-params
        (filter (fn [m] (utc/in? [0 1] (:idx m))))
        (map :kw)))

(def absolute-cases
  (tore case-params
        (filter (fn [m] (utc/in? [2 3 4 5] (:idx m))))
        (map :kw)))

(def basic-cases
  (tore case-params
        (filter (fn [m] (utc/in? [2 3 4 5 #_6 7 8 9 10] (:idx m))))
        (map :kw)))

(def all-cases
  (tore case-params
        (map :kw)))

(def ranking-cases [:p :c100k :r100k :d100k :a100k :v100k])

(def listing-cases-per-100k
  "No listing of :c100k - Closed cases per 100k"
  (tore case-params
        (filter (fn [m] (utc/in? [7 8 9] (:idx m))))
        (map :kw)))

(def listing-cases-absolute
  (->> case-params
       (filter (fn [m] (utc/in? [0 1 2] (:listing-idx m))))
       (sort-by :listing-idx)
       (map :kw)))

(defn fmt-date-fun [fmts]
  (fn [date]
    (ctf/unparse (ctf/with-zone (ctf/formatter fmts) (ctime/default-time-zone))
                 (ctc/from-date date))))

;; "Convert 2021-01-15 -> 1/15/20"
(def fmt-vaccination-date
  "(fmt-date (.parse (new java.text.SimpleDateFormat \"MM/dd/yy\")
            \"4/26/20\"))"
  (fmt-date-fun
   "M/dd/yy"
   #_"dd MMM yy"))

(def fmt-date
  "(fmt-date (.parse (new java.text.SimpleDateFormat \"MM/dd/yy\")
            \"4/26/20\"))"
  (fmt-date-fun "dd MMM yy"))

(def fmt-date-dbg
  "(fmt-date-dbg (.parse (new java.text.SimpleDateFormat \"MM/dd/yy\")
                \"4/26/20\"))"
  (fmt-date-fun "dd.MM."))

(def ^:const desc-ws
  "A placeholder"
  "")

;; TODO evaluate web services
;; https://sheets.googleapis.com/v4/spreadsheets/1jxkZpw2XjQzG04VTwChsqRnWn4-FsHH6a7UHVxvO95c/values/Dati?majorDimension=ROWS&key=AIzaSyAy6NFBLKa42yB9KMkFNucI4NLyXxlJ6jQ

;; https://github.com/iceweasel1/COVID-19-Germany

(def ^:const api-data-source
  "jhu"

  ;; csbs throws:
  ;; Execution error (ArityException) at cljplot.impl.line/eval34748$fn (line.clj:155).
  ;; Wrong number of args (0) passed to: cljplot.common/fast-max
  #_"csbs")

(def ttl
  "Time to live in (* <hours> <minutes> <seconds> <milliseconds>)."
  (* 3 60 60 1000))

(defn text-for-case [case-kw texts]
  ((comp (partial nth texts)
         first
         (partial keep-indexed (fn [i k] (when (= k case-kw) i))))
   basic-cases))

(def ^:const ^String bot-name-in-markdown
  (cstr/replace bot-name #"_" "\\\\_"))

(defn format-bytes
  "Nicely format `num-bytes` as kilobytes/megabytes/etc.
    (format-bytes 1024) ; -> 2.0 KB
  See
  https://github.com/metabase/metabase
  metabase.util/format-bytes "
  [num-bytes]
  (loop [n num-bytes [suffix & more] ["B" "kB" "MB" "GB"]]
    (if (and (seq more)
             (>= n 1024))
      (recur (/ n 1024) more)
      (format "%.1f %s" (float n) suffix))))

(defn fmap
  "See clojure.algo.generic.functor/fmap"
  [f m]
  (into (empty m) (for [[k v] m] [k (f v)])))

(defn heap-info
  "See https://github.com/metrics-clojure/metrics-clojure"
  []
  (debugf "[%s] heap %s" "heap-info"
          (let [runtime (Runtime/getRuntime)
                    ;; current size of heap in bytes
                size (.totalMemory runtime)

                    ;; maximum size of heap in bytes. The heap cannot grow
                    ;; beyond this size. Any attempt will result in an
                    ;; OutOfMemoryException.
                max-size (.maxMemory runtime)

                    ;; amount of free memory within the heap in bytes. This size
                    ;; will increase after garbage collection and decrease as
                    ;; new objects are created.
                free-size (.freeMemory runtime)]
            ((comp
              (partial fmap format-bytes))
             {:size size :max max-size :free free-size}))))

#_(def obj "clojure.core.async.impl.channels.ManyToManyChannel@490f97e1")
#_(def obj "morse.handlers$handlers$fn__65503@5fbc0011")

(defn log-obj [obj]
  (let [sobj (str obj)]
    (subs sobj (.indexOf sobj "@"))))

;; (printf "Current-ns [%s] loading %s ... done\n" *ns* 'corona.common)
