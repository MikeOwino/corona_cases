;; (printf "Current-ns [%s] loading %s ...\n" *ns* 'corona.estimate)

(ns corona.estimate
  (:require [corona.common :as com :refer
             [kclo kact kpop krec knew kdea krep kest kabs k1e5 k%%% kpop
              lense kcco]]
            [corona.macro :refer [defn-fun-id debugf infof warnf]]))

(def ^:const shift-recovery
  "Mean number of days/reports between symptoms outbreak and full recovery.
 (Lucky coincidence of 1 report per 1 day!)

  Seems like different countries have different recovery reporting policies:
  * Germany  - 14 days/reports
  * Slovakia - 23 days/reports"
  14
  #_(+ 3 (* 2 7)))

(def ^:const shift-deaths
  "https://www.spiegel.de/wissenschaft/medizin/coronavirus-infizierte-genesene-tote-alle-live-daten-a-242d71d5-554b-47b6-969a-cd920e8821f1
  Mean number of days/reports between symptoms outbreak and death. (Lucky
  coincidence of 1 report per 1 day!)"
  18)

(defn-fun-id estim-country-fn "" [calculate-fun kw-estim kw-shift-maps]
  (fn [[ccode hms-stats-country-unsorted]]
    [ccode
     (let [stats-country (sort-by :t hms-stats-country-unsorted)]
       (map
        (fn [estim stats-hm]
          (conj stats-hm
                (update-in stats-hm kw-estim (fn [_] estim))))
        (apply map calculate-fun
               (map (comp
                     (fn [{:keys [vs shift]}] (into (drop-last shift vs)
                                                   (repeat shift 0)))
                     (fn [{:keys [kw shift]}] {:vs (map (fn [stats] (get-in stats kw))
                                                       stats-country)
                                              :shift shift}))
                    kw-shift-maps))
        stats-country))]))

(defn estimate "" [pic-data]
  ((comp
    flatten
    (partial map (fn [[ccode hms]] hms))
    (partial map
             (fn [[ccode hms]]
               (let [population ((comp kpop first) hms)]
                 ((estim-country-fn (comp (fn [place] (com/per-1e5 place population))
                                          com/calc-closed)
                                    (lense kclo kest k1e5)
                                    [{:kw (lense krec kest kabs) :shift 0} ;; from kest
                                     {:kw (lense kdea krep kabs) :shift shift-deaths}])
                  [ccode hms]))))
    (partial map (estim-country-fn com/calc-closed
                                   (lense kclo kest kabs)
                                   [{:kw (lense krec kest kabs) :shift 0} ;; from kest
                                    {:kw (lense kdea krep kabs) :shift shift-deaths}]))
    (partial map
             (fn [[ccode hms]]
               (let [population ((comp kpop first) hms)]
                 ((estim-country-fn (comp (fn [place] (com/per-1e5 place population))
                                          com/calc-active)
                                    (lense kact kest k1e5)
                                    [{:kw (lense knew krep kabs) :shift 0}
                                     {:kw (lense krec kest kabs) :shift 0} ;; from kest
                                     {:kw (lense kdea krep kabs) :shift shift-deaths}])
                  [ccode hms]))))
    (partial map (estim-country-fn com/calc-active
                                   (lense kact kest kabs)
                                   [{:kw (lense knew krep kabs) :shift 0}
                                    {:kw (lense krec kest kabs) :shift 0} ;; from kest
                                    {:kw (lense kdea krep kabs) :shift shift-deaths}]))
    (partial map
             (fn [[ccode hms]]
               (let [population ((comp kpop first) hms)]
                 ((estim-country-fn (comp (fn [place] (com/per-1e5 place population))
                                          com/calc-recov)
                                    (lense krec kest k1e5)
                                    [{:kw (lense knew krep kabs) :shift shift-recovery}
                                     {:kw (lense kdea krep kabs) :shift shift-deaths}])
                  [ccode hms]))))
    (partial map (estim-country-fn com/calc-recov
                                   (lense krec kest kabs)
                                   [{:kw (lense knew krep kabs) :shift shift-recovery}
                                    {:kw (lense kdea krep kabs) :shift shift-deaths}]))
    (partial group-by kcco))
   pic-data))

;; (printf "Current-ns [%s] loading %s ... done\n" *ns* 'corona.estimate)
