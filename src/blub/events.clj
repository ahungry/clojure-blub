(ns blub.events
  (:require
   [clojure.tools.logging :as log]
   [clojure.core.async :refer [chan pub sub >!! >! <!! <! go-loop go]]))

(def input-chan (chan))
(def our-pub (pub input-chan :msg-type))
;; (def output-chan (chan))

(defn fire
  "Send in a message / payload that we'll publish to one or more listeners."
  [topic m]
  (future (>!! input-chan {:msg-type topic :data m})))

(defn listen
  "Recv a message / payload that we'll run some function against."
  [topic f]
  ;; pub topic channel
  (let [output-chan (chan)]
    (sub our-pub topic output-chan)
    (go-loop []
      (let [{:keys [data]} (<! output-chan)]
        ;; (log/info "Received an event on topic: " {:topic topic :data data})
        (f data)
        (recur)))))

(listen :foo prn)
(listen :foo (fn [x]
               (Thread/sleep 3e3)
               (prn "Hello from the second sleepy one" x)))

;; Forward to a second listener
(listen :foo (fn [x] (fire :bar x)))
(listen :foo (fn [x] (fire :odd x)))

(listen :bar (fn [x] (prn "Bar got some info!" x)))

;; Called first, it only runs code from the initial listen
(fire :foo 44)
;; If I call a second time, it then runs the second function
