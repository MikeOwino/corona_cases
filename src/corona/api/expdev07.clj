(ns corona.api.expdev07
  (:require [clojure.core.memoize :as memo]
            [clojure.set :as cset]
            [corona.core :as c :refer [read-number]]
            [corona.defs :as d])
  (:import java.text.SimpleDateFormat))

;; TODO evaluate web services
;; https://sheets.googleapis.com/v4/spreadsheets/1jxkZpw2XjQzG04VTwChsqRnWn4-FsHH6a7UHVxvO95c/values/Dati?majorDimension=ROWS&key=AIzaSyAy6NFBLKa42yB9KMkFNucI4NLyXxlJ6jQ

;; https://github.com/iceweasel1/COVID-19-Germany
(def web-service
  {:host
   "covid-tracker-us.herokuapp.com"
   #_"coronavirus-tracker-api.herokuapp.com"
   :route "/all"})

(def api-service web-service)
(def host (:host api-service))
(def url
  #_"http://127.0.0.1:5000/all"
  (str "https://" host (:route api-service)))

(def time-to-live "In minutes" 15)
(defn data [] (c/get-json url))

(def data-memo (memo/ttl data {} :ttl/threshold (* time-to-live 60 1000)))

#_(require '[ clojure.inspector :as i])
#_(i/inspect (data-memo))

(defn raw-dates-unsorted []
  #_[(keyword "2/22/20") (keyword "2/2/20")]
  (->> (data-memo) :confirmed :locations last :history keys))

(defn keyname [key] (str (namespace key) "/" (name key)))

(defn left-pad [s] (c/left-pad s 2))

(defn all-affected-country-codes
  "Countries with some confirmed, deaths or recovered cases"
  ([] (all-affected-country-codes {:limit-fn identity}))
  ([{:keys [limit-fn] :as prm}]
   (->> [:confirmed :deaths :recovered]
        (map (fn [case] (->> (data-memo)
                            case :locations
                            (map :country_code)
                            set)))
        (reduce cset/union)
        (mapv (fn [cc] (if (= "XX" cc)
                        d/default-2-country-code
                        cc)))
        distinct
        limit-fn)))

(defn raw-dates []
  (->> (raw-dates-unsorted)
       (map keyname)
       (map (fn [date] (re-find (re-matcher #"(\d+)/(\d+)/(\d+)" date))))
       (map (fn [[_ m d y]]
              (->> [y m d]
                   (map left-pad)
                   (interpose "/")
                   (apply str))))
       sort
       (map (fn [kw] (re-find (re-matcher #"(\d+)/(\d+)/(\d+)" kw))))
       (map (fn [[_ y m d]]
              (->> [m d y]
                   (map read-number)
                   (interpose "/")
                   (apply str)
                   (keyword))))))

(defn sums-for-date [case locations raw-date]
  (if (and (empty? locations)
           (= :recovered case))
    0
    (transduce (map (comp
                     ;; https://github.com/ExpDev07/coronavirus-tracker-api/issues/41
                     ;; str read-number
                     raw-date
                     :history))
               + 0
               locations)))

(defn pred-fn [country-code]
  (fn [loc]
    (condp = country-code
      d/worldwide-2-country-code
      true

      d/default-2-country-code
      ;; XX comes from the service
      (= "XX" (:country_code loc))

      (= country-code (:country_code loc)))))

(defn sums-for-case
  "Return sums for a given `case` calculated for every single day
  E.g.
  (sums-for-case {:case :confirmed :pred (pred-fn \"SK\")})
  "
  [{:keys [case pred]}]
  (let [locations
        (filter pred (:locations (case (data-memo))))
        #_((comp (filter pred) :locations case) (data-memo))
        #_(->> (data-memo) case :locations (filter pred))
        ]
    (map (fn [raw-date]
           (sums-for-date case locations raw-date))
         (raw-dates))))

(defn get-counts
  "Examples:
  (get-counts {:pred (fn [_] true)})
  (get-counts {:pred (pred-fn \"SK\")})
  "
  [prm]
  (let [crd (mapv (fn [case] (sums-for-case (conj prm {:case case})))
                  [:confirmed :recovered :deaths])]
    (zipmap [:c :r :d :i] (conj crd (apply mapv c/calculate-ill crd)))))

(defn confirmed [prm] (:c (get-counts prm)))
(defn deaths    [prm] (:d (get-counts prm)))
(defn recovered [prm] (:r (get-counts prm)))
(defn ill       [prm] (:i (get-counts prm)))

(defn dates
  ([] (dates {:limit-fn identity}))
  ([{:keys [limit-fn] :as prm}]
   (let [sdf (new SimpleDateFormat "MM/dd/yy")]
     (map (fn [rd] (.parse sdf (keyname rd)))
          (limit-fn (raw-dates))))))

#_(def dates-memo (memo/memo dates))

(defn get-last [coll] (first (take-last 1 coll)))

(defn get-prev [coll] (first (take-last 2 coll)))

(defn eval-fun [{:keys [fun] :as prm}]
  (conj {:f (fun (dates))}
        (zipmap [:c :d :r :i]
                (let [case-counts (get-counts prm)]
                  (map fun
                       ;; TODO use select-keys or something similar
                       [(:c case-counts)
                        (:d case-counts)
                        (:r case-counts)
                        (:i case-counts)])))))

(defn delta
  "Example (delta {:pred (pred-fn \"CN\")})"
  [prm]
  (->> [get-prev get-last]
       (map (fn [fun] (eval-fun (assoc prm :fun fun))))
       (apply (fn [prv lst]
                (map (fn [k]
                       {k (- (k lst) (k prv))})
                     [:c :d :r :i])
                ))
       (reduce into {})))

(defn last-day
  "Example (last-day {:pred (fn [_] true)})"
  [prm] (eval-fun (assoc prm :fun get-last)))
