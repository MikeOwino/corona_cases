;; (printf "Current-ns [%s] loading %s ...\n" *ns* 'corona.msg.text.lists)

(ns corona.msg.text.lists
  (:require [clojure.string :as cstr]
            [corona.api.cache :as cache]
            [corona.api.expdev07 :as data]
            [corona.common :as com]
            [corona.countries :as ccr]
            [corona.country-codes :as ccc]
            [corona.lang :as lang]
            [corona.macro :refer [defn-fun-id debugf]]
            [corona.msg.text.common :as msgc]
            [taoensso.timbre :as timbre]))

(def ^:const cnt-messages-in-listing
  "nr-countries / nr-patitions : 126 / 6, 110 / 5, 149 / 7"
  7)

(defn get-from-cache! "" [case-kw {:keys [msg-idx json fun] :as prm}]
  ((comp
    (fn [prm]
      (let [full-kws [:list ((comp keyword :name meta find-var) fun) case-kw]]
        (cond
          msg-idx
          ((comp
            (partial cache/from-cache! (fn [] ((eval fun) case-kw prm)))
            (partial conj full-kws)
            keyword
            str)
           msg-idx)

          prm
          ((comp
            vals
            (partial cache/from-cache! (fn [] ((eval fun) case-kw prm))))
           full-kws)

          :else
          ((comp
            vals
            (partial get-in @cache/cache))
           full-kws)))))
   prm))

(defn-fun-id calc-listings "" [case-kws json fun]
  (let [
        stats (data/stats-countries json)
        footer (msgc/footer com/html)
        header ((comp
                 (partial msgc/header com/html)
                 :t
                 data/last-report
                 (fn [pred-hm] (assoc pred-hm :json json))
                 data/create-pred-hm
                 ccr/get-country-code)
                ccc/worldwide)
        prm {:json json :header header :footer footer
             :cnt-reports (count (data/dates json))}]
    ((comp
    (partial
     run!
     (fn [case-kw]
       (let [coll (sort-by case-kw < stats)
             ;; Split the long list of all countries into smaller sub-parts
             sub-msgs (partition-all (/ (count coll)
                                        cnt-messages-in-listing) coll)
             sub-msgs-prm (assoc prm
                                 :cnt-msgs (count sub-msgs))]
         ((comp
           doall
           (partial map-indexed
                    (fn [idx sub-msg] (get-from-cache!
                                      case-kw (assoc sub-msgs-prm
                                                     :msg-idx (inc idx)
                                                     :data sub-msg
                                                     :fun fun)))))
          sub-msgs)))))
   case-kws)))

(defn-fun-id absolute-vals
  "Listing commands in the message footer correspond to the columns in the
  listing. See also `footer`, `bot-father-edit`."
  [case-kw {:keys [msg-idx cnt-msgs data cnt-reports header footer]}]
  #_(debugf "case-kw %s" case-kw)
  (let [spacer " "
        sort-indicator "▴" ;; " " "▲"
        omag-active 7 ;; order of magnitude i.e. number of digits
        omag-recov  (inc omag-active)
        omag-deaths (dec omag-active)

        msg
        (format
         (msgc/format-linewise
          [["%s\n"   [header]]
           ["%s\n"   [(format "%s %s;  %s/%s"
                              lang/report cnt-reports msg-idx cnt-msgs)]]
           ["    %s " [(str lang/active    (if (= :a case-kw) sort-indicator " "))]]
           ["%s"     [spacer]]
           ["%s "    [(str lang/recovered (if (= :r case-kw) sort-indicator " "))]]
           ["%s"     [spacer]]
           ["%s\n"   [(str lang/deaths    (if (= :d case-kw) sort-indicator " "))]]
           ["%s"     [(str
                       "%s"   ; listing table
                       "%s"   ; sorted-by description; has its own new-line
                       "\n\n"
                       "%s"   ; footer
                       )]]])
         ((comp
           (partial cstr/join "\n")
           (partial map (fn [{:keys [a r d ccode]}]
                          (let [cname (ccr/country-name-aliased ccode)]
                            (format "<code>%s%s%s%s%s %s</code>  %s"
                                    (com/left-pad a " " omag-active)
                                    spacer
                                    (com/left-pad r " " omag-recov)
                                    spacer
                                    (com/left-pad d " " omag-deaths)
                                    (com/right-pad cname 17)
                                    (cstr/lower-case (com/encode-cmd ccode)))))))
          data)
         ""
         footer)]
    (debugf "case-kw %s msg-idx %s msg-size %s"
            case-kw msg-idx (com/measure msg))
    msg))

(defn-fun-id per-100k
  "Listing commands in the message footer correspond to the columns in the
  listing. See also `footer`, `bot-father-edit`."
  [case-kw {:keys [msg-idx cnt-msgs data cnt-reports header footer]}]
  #_(debugf "case-kw %s" case-kw)
  (let [spacer " "
        sort-indicator "▴" ;; " " "▲"
        ;; omag - order of magnitude i.e. number of digits
        omag-active-per-100k 4
        omag-recove-per-100k omag-active-per-100k
        omag-deaths-per-100k (dec omag-active-per-100k)
        msg
        (format
         (msgc/format-linewise
          [["%s\n" [header]]
           ["%s\n" [(format "%s %s;  %s/%s"
                            lang/report cnt-reports msg-idx cnt-msgs)]]
           ["%s "  [(str lang/active-per-1e5
                         (if (= :a100k case-kw) sort-indicator " "))]]
           ["%s"   [spacer]]
           ["%s "  [(str lang/recove-per-1e5
                         (if (= :r100k case-kw) sort-indicator " "))]]
           ["%s"   [spacer]]
           ["%s"   [(str lang/deaths-per-1e5
                         (if (= :d100k case-kw) sort-indicator " "))]]
           ["\n%s" [(str
                     "%s"     ; listing table
                     "%s"     ; sorted-by description; has its own new-line
                     "\n\n%s" ; footer
                     )]]])
         ((comp
           (partial cstr/join "\n")
           (partial map (fn [{:keys [a100k r100k d100k ccode]}]
                          (let [cname (ccr/country-name-aliased ccode)]
                            #_(debugf "case-kw %s, cname %s" case-kw cname)
                            (format "<code>   %s%s   %s%s    %s %s</code>  %s"
                                    (com/left-pad a100k " " omag-active-per-100k)
                                    spacer
                                    (com/left-pad r100k " " omag-recove-per-100k)
                                    spacer
                                    (com/left-pad d100k " " omag-deaths-per-100k)
                                    (com/right-pad cname 17)
                                    (cstr/lower-case (com/encode-cmd ccode)))))))
          data)
         ""
         footer)]
    (debugf "case-kw %s msg-idx %s msg-size %s"
            case-kw msg-idx (com/measure msg))
    msg))

(defmulti  list-cases (fn [listing-cases-per-100k?] listing-cases-per-100k?))

(defmethod list-cases true [_]
  (fn [case-kw & [json
                 msg-idx prm]]
    ((comp
      #_(fn [r] (timbre/debugf "[list-cases true] r %s" r) r)
      (partial get-from-cache! case-kw))
     (assoc prm
            :msg-idx msg-idx
            :json json
            :fun 'corona.msg.text.lists/per-100k))))

(defmethod list-cases false [_]
  (fn [case-kw & [json
                 msg-idx prm]]
    #_(timbre/debugf "[list-cases false] msg-idx %s" msg-idx)
    (get-from-cache! case-kw (assoc prm
                                    :msg-idx msg-idx
                                    :json json
                                    :fun 'corona.msg.text.lists/absolute-vals))))

;; (printf "Current-ns [%s] loading %s ... done\n" *ns* 'corona.msg.text.lists)
