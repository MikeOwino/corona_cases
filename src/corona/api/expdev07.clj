(ns corona.api.expdev07
  (:require
   [clojure.set :as cset]
   [corona.common :as co]
   [corona.countries :as cr]
   [corona.country-codes :as cc :refer :all]
   [utils.core :refer [dbgv dbgi] :exclude [id]]
   [taoensso.timbre :as timbre :refer :all]
   )
  (:import java.text.SimpleDateFormat))

(def ^:const url (format "http://%s/all" co/api-server))

(defn data [] (co/get-json url))

(defonce cache (atom nil))

(defn request! []
  (doall
   (let [tbeg (System/currentTimeMillis)]
     (let [response (data)]
       (swap! cache (fn [_] response))
       (debug (format "[request!] %s chars cached in %s ms"
                      (count (str @cache))
                      (- (System/currentTimeMillis) tbeg)))))))

(def data-memo
  (fn [] @cache)
  #_(co/memo-ttl data))

(defn raw-dates-unsorted []
  #_[(keyword "2/22/20") (keyword "2/2/20")]
  (keys (:history (last (:locations (:confirmed (data-memo)))))))

(defn keyname [key] (str (namespace key) "/" (name key)))

(defn left-pad [s] (co/left-pad s 2))

#_(require '[ clojure.inspector :as i])
#_(i/inspect
   (data-with-pop-memo)
   #_(data-memo))

(defn xf-sort
  "A sorting transducer. Mostly a syntactic improvement to allow composition of
  sorting with the standard transducers, but also provides a slight performance
  increase over transducing, sorting, and then continuing to transduce.

  Thanks to https://gist.github.com/matthewdowney/380dd28c1046d4919a8c59a523f804fd.js
  "
  ([]
   (xf-sort compare))
  ([cmp]
   (fn [rf]
     (let [temp-list (java.util.ArrayList.)]
       (fn
         ([]
          (rf))
         ([xs]
          (reduce rf xs (sort cmp (vec (.toArray temp-list)))))
         ([xs x]
          (.add temp-list x)
          xs))))))

(defn raw-dates []
  (transduce
   (comp
    (map keyname)
    (map (fn [date] (re-find (re-matcher #"(\d+)/(\d+)/(\d+)" date))))
    (map (fn [[_ m d y]]
           (transduce (comp (map left-pad)
                            (interpose "/"))
                      str
                      [y m d])))
    (xf-sort)
    (map (fn [kw] (re-find (re-matcher #"(\d+)/(\d+)/(\d+)" kw))))
    (map (fn [[_ y m d]]
           (keyword
            (transduce (comp (map co/read-number)
                             (interpose "/"))
                       str
                       [m d y])))))
   conj []
   (raw-dates-unsorted)))

(defn population-cnt [country-code]
  (or (get cr/population country-code)
      ;; world population is the sum
      ;; 7792480951
      (let [default-population 0]
        (error (format "population nr unknown; country-code: %s; using %s"
                       country-code
                       default-population))
        default-population)))

(defn data-with-pop
  "Data with population numbers"
  []
  (conj
   (data-memo)
   {:population
    {:locations
     (let [dates (raw-dates)]
       (->> (cc/all-country-codes)
            ;; (take 0)
            (mapv (fn [country-code]
                    {
                     :country (cr/country-name-aliased country-code)
                     :country_code country-code
                     :history
                     ;; {:1/23/20 1e6 ;; start number
                     ;;    ;; other days - calc diff
                     ;; }
                     (let [pop-cnt (population-cnt country-code)]
                       (zipmap dates (repeat pop-cnt)))}))))}}))

(def data-with-pop-memo
  (co/memo-ttl data-with-pop))

(defn all-affected-country-codes
  "Countries with some confirmed, deaths or recovered cases"
  ([] (all-affected-country-codes {:limit-fn identity}))
  ([{:keys [limit limit-fn] :as prm}]
   (let [coll (transduce (map (fn [case]
                                #_(set (map :country_code (:locations (case
                                                                          (data-with-pop-memo)
                                                                          #_(data-memo)))))
                                (transduce (map :country_code)
                                           conj #{}
                                           ((comp :locations case)
                                            (data-with-pop-memo)
                                            #_(data-memo)))))
                         cset/union #{}
                         [:population :confirmed :deaths :recovered])]
     (transduce (comp (map (fn [cc] (if (= cc/xx cc)
                                     cc/default-2-country-code
                                     cc)))
                      (distinct)
                      limit-fn)
                conj []
                coll))

   #_(->> [:population :confirmed :deaths :recovered]
          (map (fn [case] (->>
                            (data-with-pop-memo)
                            #_(data-memo)
                            case :locations
                            (map :country_code)
                            set)))
        (reduce cset/union)
        (mapv (fn [cc] (if (= xx cc)
                        d/default-2-country-code
                        cc)))
        (distinct)
        (limit-fn)
        )))

(def all-affected-country-codes-memo
  (co/memo-ttl all-affected-country-codes))

(defn dates
  ([] (dates {:limit-fn identity}))
  ([{:keys [limit-fn] :as prm}]
   #_(debug "dates" {:limit-fn limit-fn})
   (let [sdf (new SimpleDateFormat "MM/dd/yy")]
     (map (fn [rd] (.parse sdf (keyname rd)))
          (limit-fn (raw-dates))))))

(def dates-memo
  (co/memo-ttl dates))

(defn get-last [coll] (first (take-last 1 coll)))

(defn get-prev [coll] (first (take-last 2 coll)))

(defn sums-for-date [case locations raw-date]
  (if (and (empty? locations)
           (= :recovered case))
    0
    (transduce (map (comp
                     ;; https://github.com/ExpDev07/coronavirus-tracker-api/issues/41
                     ;; str co/read-number
                     raw-date
                     :history))
               + 0
               locations)))

(defn sums-for-case
  "Return sums for a given `case` calculated for every single day. E.g.
  (sums-for-case {:case :confirmed :pred (pred-fn sk)})
  "
  [{:keys [case pred]}]
  (let [locations (filter pred
                          ((comp :locations case)
                           (data-with-pop-memo)
                           #_(data-memo)))]
    (map (fn [raw-date]
           (sums-for-date case locations raw-date))
         (raw-dates))))

(defn get-counts
  "Returns a hash-map containing case-counts day-by-day. E.g.:
  (get-counts {:pred-q '(pred-fn sk) :pred (pred-fn sk)})
  ;; => ;; last 5 values
  {
   :p (... 5456362 5456362 5456362 5456362 5456362)
   :c (...    2566    2596    2599    2615    2690)
   :r (...    1861    1864    1866    1874    1884)
   :d (...      31      31      31      31      31)
   :i (...     674     701     702     710     775)}

  (get-counts {:pred-q '(pred-fn true) :pred (fn [_] true)})
  "
  [prm]
  (let [pcrd (mapv (fn [case] (sums-for-case (conj prm {:case case})))
                   [:population :confirmed :recovered :deaths])]
    (zipmap co/all-cases
            (apply
             conj pcrd
             (->> [co/calculate-active
                   (co/calculate-cases-per-100k :i)
                   (co/calculate-cases-per-100k :r)
                   (co/calculate-cases-per-100k :d)
                   (co/calculate-cases-per-100k :c)]
                  #_(mapv (fn [f] (apply mapv f pcrd)))
                  (mapv (fn [f] (apply mapv (fn [p c r d]
                                             (->> [p c r d]
                                                  (zipmap [:p :c :r :d])
                                                  (f)))
                                      pcrd))))))))

(def get-counts-memo
  #_get-counts
  (co/memo-ttl get-counts))

(defn population [prm]
  #_(debug "population" prm)
  (:p (get-counts-memo prm)))

(defn confirmed [prm]
  #_(debug "confirmed" prm)
  (:c (get-counts-memo prm)))

(defn deaths [prm]
  #_(debug "deaths" prm)
  (:d (get-counts-memo prm)))

(defn recovered [prm]
  #_(debug "recovered" prm)
  (:r (get-counts-memo prm)))

(defn active [prm]
  #_(debug "active" prm)
  (:i (get-counts-memo prm)))

(defn active-per-100k [prm]
  #_(debug "active-per-100k" prm)
  (:i100k (get-counts-memo prm)))

(defn recovered-per-100k [prm]
  #_(debug "recovered-per-100k" prm)
  (:r100k (get-counts-memo prm)))

(defn deaths-per-100k [prm]
  #_(debug "deaths-per-100k" prm)
  (:d100k (get-counts-memo prm)))

(defn closed-per-100k [prm]
  #_(debug "closed-per-100k" prm)
  (:c100k (get-counts-memo prm)))


(defn eval-fun
  "E.g.:
  (eval-fun {:fun get-last :pred-q '(pred-fn sk) :pred (pred-fn sk)})
  (eval-fun {:fun get-last :pred-q '(fn [_] true) :pred (fn [_] true)})
  "
  [{:keys [fun date] :as prm}]
  (into {:f (fun (dates-memo))}
        (map (fn [[k v]] {k (fun v)})
             (get-counts-memo prm))))

(defn delta
  "E.g.:
  (delta {:pred-q '(pred-fn cn)  :pred (pred-fn cn)})
  (delta {:pred-q '(fn [_] true) :pred (fn [_] true)})"
  [prm]
  (->> [get-prev get-last]
       (map (fn [fun] (eval-fun (assoc prm :fun fun))))
       (apply (fn [prv lst]
                (map (fn [k]
                       {k (- (k lst) (k prv))})
                     co/all-cases)))
       (reduce into {})))

(defn last-day
  "E.g.:
  (last-day {:pred-q '(pred-fn sk) :pred (pred-fn sk)})
  (last-day {:pred-q '(fn [_] true) :pred (fn [_] true)})"
  [prm]
  (eval-fun (assoc prm :fun get-last)))

(defn last-8-reports
  "E.g.:
  (last-8-reports {:pred-q '(pred-fn sk) :pred (pred-fn sk)})
  (last-8-reports {:pred-q '(fn [_] true) :pred (fn [_] true)})"
  [prm]
  (eval-fun (assoc prm :fun (fn [coll] (take-last 8 coll)))))

(defn pred-fn [country-code]
  (fn [loc]
    (condp = country-code
      cc/worldwide-2-country-code
      true

      cc/default-2-country-code
      ;; XX comes from the service
      (= cc/xx (:country_code loc))

      (= country-code (:country_code loc)))))

(defn stats-per-country [{:keys [cc] :as prm}]
  (conj
   (last-day (assoc prm :pred-q '(pred-fn cc) :pred (pred-fn cc)))
   #_{:cn (cr/country-name-aliased cc)}
   {:cc cc}))

(defn stats-all-affected-countries [prm]
  (map (fn [cc]
         (stats-per-country (assoc prm :cc cc)))
       (all-affected-country-codes-memo)))

(def stats-all-affected-countries-memo
  #_stats-all-affected-countries
  (co/memo-ttl stats-all-affected-countries))
