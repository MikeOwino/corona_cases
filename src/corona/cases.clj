;; (printf "Current-ns [%s] loading %s ...\n" *ns* 'corona.cases)

(ns corona.cases
  (:require
   [utils.core :as utc]
   [corona.models.dbase :as dbase]
   [taoensso.timbre :as timbre]
   [corona.macro :refer [defn-fun-id debugf infof warnf]]
   [corona.common :refer
    [ktst kpop kvac kact krec kdea knew kclo kdea
     ka1e5 kr1e5 kc1e5 kd1e5 kv1e5
     ]]))

(defmacro tore
  "->>-or-eduction. In fact both have the same performance.
  See also:
  - my explanations at https://clojuredocs.org/clojure.core/eduction
  - https://github.com/rplevy/swiss-arrows"
  [coll & fns]
  `(->> ~coll ~@fns)
  #_`(sequence (eduction ~@fns ~coll)))

(defn threshold-defaults-sql
  "Sql commands for threshold-defaults"
  []
  (printf "
insert into \"thresholds\" (kw,inc,val,updated_at) values ('a',10000,5119000,cast('now()' as timestamp(0)));
insert into \"thresholds\" (kw,inc,val,updated_at) values ('d',1000,134000,cast('now()' as timestamp(0)));
insert into \"thresholds\" (kw,inc,val,updated_at) values ('n',50000,5260000,cast('now()' as timestamp(0)));
insert into \"thresholds\" (kw,inc,val,updated_at) values ('r',10000,4087000,cast('now()' as timestamp(0)));
\n
update \"thresholds\" set val = %s, updated_at = cast('now()' as timestamp(0)) where kw = 'a';
update \"thresholds\" set val = %s, updated_at = cast('now()' as timestamp(0)) where kw = 'd';
update \"thresholds\" set val = %s, updated_at = cast('now()' as timestamp(0)) where kw = 'n';
update \"thresholds\" set val = %s, updated_at = cast('now()' as timestamp(0)) where kw = 'r';
select * from thresholds order by kw;
" 640390 138000 6210000 6037000))

(def threshold-defaults
  "Recovery data is not provided anymore. So:

On the master branch:
postgres=# select * from thresholds order by kw;
kw |  inc  |   val   |     updated_at
----+-------+---------+---------------------
a  | 10000 | 6049000 | 2021-12-21 01:03:29
d  |  1000 |  136000 | 2021-12-21 00:50:50
n  | 50000 | 6210000 | 2021-12-21 00:50:29
r  | 10000 | 3447000 | 2021-12-21 00:50:40

Old values on corona-cases-bot:
kw |  inc  |   val   |     updated_at
----+-------+---------+---------------------
a  | 10000 | 5119000 | 2021-09-13 05:31:32
d  |  1000 |  134000 | 2021-06-17 05:21:14
n  | 50000 | 5260000 | 2021-09-10 04:25:13
r  | 10000 | 4087000 | 2021-07-30 05:24:33

Old values on corona-cases-bot:
kw |  inc  |   val   |     updated_at
----+-------+---------+---------------------
a  | 10000 | 6069000 | 2021-12-30 08:16:53
d  |  1000 |  138000 | 2021-12-30 08:16:52
n  | 50000 | 6210000 | 2021-12-16 00:48:21
r  | 10000 | 4057000 | 2021-12-26 05:28:27

Old values on hokuspokus:
kw |  inc  |   val   |     updated_at
----+-------+---------+---------------------
a  | 10000 | 5119000 | 2021-09-12 21:26:43
d  |  1000 |  134000 | 2021-06-23 05:23:34
n  | 50000 | 5260000 | 2021-09-09 17:27:41
r  | 10000 | 3867000 | 2021-07-27 05:22:40"
  ((comp (partial sort-by :kw))
   [{:kw kvac :inc (int 1e6) :val (int 1e7)}
    {:kw kpop :inc (int 1e6) :val (int 1e7)}
    {:kw knew :inc 50000     :val (int 3660e3)}
    {:kw krec :inc 10000     :val (int 2937e3)}
    {:kw kdea :inc 1000      :val (int 87e3)}
    {:kw kact :inc 10000     :val (int 1029e3)}]))

(defn norm [raw-ths]
  ((comp
    (fn [ths] (def ths ths) ths)
    (partial map
             (comp
              (fn [m] (update-in m [:kw] keyword))
              (fn [m]
                (clojure.set/rename-keys
                 m
                 {:thresholds/kw :kw
                  :thresholds/inc :inc
                  :thresholds/val :val
                  :thresholds/updated_at :updated_at}))))
    (fn [ths] (def raw-ths ths) ths))
   raw-ths))

(def case-params
  [
   ;; absolute values
   {:idx  0 :kw kvac}
   {:idx  1 :kw kpop}
   {:idx  2 :kw knew}
   {:idx  3 :kw krec :listing-idx 1}
   {:idx  4 :kw kdea :listing-idx 2}
   {:idx  5 :kw kact :listing-idx 0}

   ;; TODO the order matters: it must be the same as in the info-message
   ;; Incidence per 1e5
   {:idx  6 :kw kv1e5}
   {:idx  7 :kw ka1e5}
   {:idx  8 :kw kr1e5}
   {:idx  9 :kw kd1e5}
   {:idx 10 :kw kc1e5}  ;; closed-per-1e5

   ;; rates
   {:idx 11 :kw :v%}
   {:idx 12 :kw :a%}
   {:idx 13 :kw :r%}
   {:idx 14 :kw :d%}
   {:idx 15 :kw :c%}     ;; closed-rate
   ])

(def aggregation-params
  ":idx - defines an order in appearance"
  [{:idx  0 :kw :sum}
   {:idx  1 :kw :abs}])

(def aggregation-cases
  (tore aggregation-params
        (filter (fn [m] (utc/in? [0 1] (:idx m))))
        (map :kw)))

(def absolute-cases
  (tore case-params
        (filter (fn [m] (utc/in? [2 3 4 5] (:idx m))))
        (map :kw)))

(def cartesian-product-all-case-types
  (for [a aggregation-cases
        b absolute-cases]
    [a b]))

(def basic-cases
  (tore case-params
        (filter (fn [m] (utc/in? [0 1 2 3 4 5 #_6 7 8 9 10] (:idx m))))
        (map :kw)))

(def all-report-cases
  (tore case-params
        (filter (fn [m] (utc/in? [0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15] (:idx m))))
        (map :kw)))

(def all-cases
  (tore case-params
        (map :kw)))

(def ranking-cases
  (tore case-params
        (filter (fn [m] (utc/in? [1 6 7 8 9 10] (:idx m))))
        (mapv :kw)))

(def listing-cases-per-1e5
  (tore case-params
        (filter (fn [m] (utc/in? [7 8 9] (:idx m))))
        (map :kw)))

(def listing-cases-absolute
  ((comp
    (partial map :kw)
    (partial sort-by :listing-idx)
    (partial filter (fn [m] (utc/in? [0 1 2] (:listing-idx m)))))
   case-params))

(defn text-for-case
  "(text-for-case case-kw texts)"
  [case-kw texts]
  ((comp (partial nth texts)
         first
         (partial keep-indexed (fn [i k] (when (= k case-kw) i))))
   basic-cases))

;; (printf "Current-ns [%s] loading %s ... done\n" *ns* 'corona.cases)
