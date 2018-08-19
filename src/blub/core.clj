(ns blub.core
  (:gen-class))

(defn add [a b]
  (+ a b))

(reduce add [1 2 3 4])

(defn to-int [n]
  (Integer. n))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]

  (->> args (map to-int) (reduce add) println)

  ;; (println (reduce add (map to-int args)))
  (println "Hello, World!"))


;; Add some types
;; https://github.com/clojure/core.typed

;; CLI interface
;; https://multimud.github.io/clojure-lanterna/
;; (require '[lanterna.screen :as s])

;; (def scr (s/get-screen))

;; (s/start scr)

;; (s/put-string scr 10 10 "Hello, world!")
;; (s/put-string scr 10 11 "Press any key to exit!")
;; (s/redraw scr)
;; (s/get-key-blocking scr)

;; (s/stop scr)
