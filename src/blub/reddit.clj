(ns reddit
  (:require [clojure.core.async :as a]))

(def my-data-log (atom []))

(defn fetch-data! [n]
  ;; (Thread/sleep 1e3)
  (burner)
  (swap! my-data-log conj (str "Fetched record: " n))
  n)

(defn pmap!
  ([n f xs]
   (let [output-chan (a/chan)]
     (a/pipeline-blocking n
                          output-chan
                          (map f)
                          (a/to-chan xs))
     (a/<!! (a/into [] output-chan))))
  ([f xs] (pmap! (.availableProcessors (Runtime/getRuntime)) xs)))

(defn work! [n]
  (pmap! n fetch-data! (range 100)))

;;reddit> (time (count (work! 100)))
;;"Elapsed time: 1013.54152 msecs"
