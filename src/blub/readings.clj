;; http://rosettacode.org/wiki/Text_processing/1#Common_Lisp

(ns blub.readings)

(defn get-text [] (slurp "/home/mcarter/src/rosettacode/readings.txt"))

(defn make-metrics [xs]
  (map (fn [[x y]] {:val x :flag y})
       (partition 2 xs)))

;; Measurements at 01:00 02:00 up to 24:00
;; date val1 flag1 val2 flag2 ... val24 flag24
(defn parse-line [s]
  (let [tuple (clojure.string/split s #"\t")]
    {:day (first tuple)
     :metrics (make-metrics (into [] (map read-string (rest tuple))))}))

(defn parse-file [s]
  (->> (clojure.string/split s #"\r\n")
       (map parse-line)))

(defn is-valid? [{:keys [flag ]}] (> flag 0))

(defn get-stats [{:keys [metrics]}]
  (let [xs (filter is-valid? metrics)
        total (count xs)
        sum (reduce + (map :val xs))]
    {:mean (/ sum total)
     :total sum
     :accept total
     :reject (- (count metrics) total)}))

(defn -main []
  (->> (get-text)
       parse-file
       (map get-stats)
       (take 10)))
