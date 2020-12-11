;; https://clojure.github.io/core.typed/#clojure.core.typed/List
;; https://github.com/gnl/ghostwheel#how-to-read-a-readme

(ns blub.core
  #:ghostwheel.core{:check true
                    :num-tests 10}
  ;; (:require [clojure.core.typed :as t])
  ;; (:require [clojure.core.typed :as t :refer [ann check-ns]])
  (:require
   [blub.my-types :as mt]
   [blub.events :as e]
   )
  (:require [clojure.spec.alpha :as s])
  (:require [clojure.spec.gen.alpha :as gen])
  (:require [clojure.spec.test.alpha :as stest])
  (:require [ghostwheel.core :as g
             :refer [>defn >defn- >fdef => | <- ?]])
  (:require
   [cheshire.core :as json]
   [udp-wrapper.core :as udp])
  (:require
   [xdg-rc.core :as xdg-rc])
  (:gen-class))

;; https://clojure.org/about/spec
(s/def ::even? (s/and integer? even?))
(s/def ::odd? (s/and integer? odd?))
(s/def ::a integer?)
(s/def ::b integer?)
(s/def ::c integer?)
(def s (s/cat :forty-two #{42}
              :odds (s/+ ::odd?)
              :m (s/keys :req-un [::a ::b ::c])
              :oes (s/* (s/cat :o ::odd? :e ::even?))
              :ex (s/alt :odd ::odd? :even ::even?)
              ))
(s/conform s [42 11 13 15 {:a 1 :b 2 :c 3} 1 2 3 42 43 44 11])

;; https://clojure.org/guides/spec
(def email-regex #".*@.*")
(defn email-type-gen []
  (gen/fmap #(str "m+" % "@ahungry.com") (gen/string-alphanumeric)))

(s/def ::email-type
  (s/with-gen (s/and string? #(re-matches email-regex %))
    email-type-gen))

(gen/generate (s/gen ::email-type))
(gen/sample (s/gen ::email-type))

(s/def ::acctid int?)
(s/def ::first-name string?)
(s/def ::last-name string?)
(s/def ::email ::email-type)

(defn phone-type-gen []
  (gen/fmap #(read-string (str "555555" %))
            (s/gen (set (map #(+ 1000 %) (range 9999))))))
(s/def ::phone
  (s/with-gen
    (s/and int?
           #(> % 1000000000)
           #(< % 9999999999))
    phone-type-gen))

(s/def ::person (s/keys :req [::first-name ::last-name ::email]
                        :opt [::phone]))

(s/def ::business (s/keys :req [::email ::phone]))
(s/def ::businesses (s/coll-of ::business :into [] :count 1))
(s/def ::businessesx (s/every ::business :into [] :max-count 5 :min-count 1))

(s/def ::customer (s/keys :req [::person ::business]))

(gen/generate (s/gen ::person))

(defn sample-person []
  (gen/sample (s/gen ::person)))

(defn sample-business []
  (gen/sample (s/gen ::business)))

(defn sample-customer []
  (gen/sample (s/gen ::customer)))

(s/valid? ::person
          {::first-name "Matthew"
           ::last-name "Carter"
           ::email "m@ahungry.com"})

(s/def :unq/person
  (s/keys :req-un [::first-name ::last-name ::email]
          :opt-un [::phone]))

(s/def :unq/business
  (s/keys :req-un [::email ::phone]))
(s/def :unq/businesses (s/coll-of :unq/business :into [] :count 1))

(s/def :unq/customer (s/keys :req-un [:unq/person :unq/businesses]))

(defn unq-sample-customer []
  (gen/sample (s/gen :unq/customer)))

(defn unq-generate-customer []
  (gen/generate (s/gen :unq/customer)))

(s/conform :unq/person
           {:first-name "Matthew"
            :last-name "Carter"
            :email "m@ahungry.com"})

(defrecord Person [first-name last-name email phone])

(s/explain :unq/person (->Person "Matthew" nil nil nil))

(defn what-can-i-do [] (keys (ns-publics 'ghostwheel.core)))

(>defn xranged-rand
       "Taken from a guide..."
       [start end]
       [int? int? | #(< start end) => int? | #(>= % start) #(< % end)]
       (+ start (long (rand (- end start)))))

(defn ranged-rand
  "Taken from the guide."
  [start end]
  (+ start (long (rand (- end start)))))
(s/fdef ranged-rand
  :args (s/and (s/cat :start int? :end int?)
               #(< (:start %) (:end %)))
  :ret int?
  :fn (s/and #(>= (:ret %) (-> % :args :start))
             #(< (:ret %) (-> % :args :end)))
  )

(>defn addition
       [a b]
       [pos-int? pos-int? => int? | #(> % a) #(> % b)]
       (- a b))

(s/exercise-fn `addition)
(stest/instrument `addition)

;; https://clojure.org/guides/spec#_testing
(stest/check `addition)

;; Test all the things
(defn test-all [] (-> (stest/enumerate-namespace 'blub.core) stest/check))
(test-all)

(defn add [a b]
  (+ a b))

(defn to-int [^String n]
  (Integer. n))

(defn radd [n]
  (reduce add n))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]

  (->> args (map to-int) radd println)

  (g/check)

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

;; Business logic
;; Get a user based on matching id.
;; If they're over 18, and favorite language is this one,
;; then send them a job offer invite.
(defrecord UserModel [name age id language])

(s/def ::positive-int (s/and int? #(> % 0)))
(s/def ::full-string (s/and string? #(> (count %) 0)))
(s/def ::name ::full-string)
(s/def ::age ::positive-int)
(s/def ::id ::positive-int)
(s/def ::language ::full-string)
(s/def ::user-model (s/keys :req-un [::name ::age ::id ::language ]))

(defn get-user-model
  "Get a valid instance of the record.  Throws message such as:

  In: [:id] val: -1 fails spec: :blub.core/positive-int at: [:id] predicate: (> % 0)

  on failure."
  [name age id language]
  (let [model (->UserModel name age id language)]
    (if (s/valid? ::user-model model) model
        (throw (Throwable. (str (s/explain ::user-model model)))))))

;; conform, explain, valid?
(s/valid? ::user-model (->UserModel "Carter" 36 2 "Clojure"))

(defn get-users [_id]
  [(->UserModel "Carter" 36 2 "Clojure")])

(defmulti adult-user? class)
(defmethod adult-user? UserModel [{:keys [age]}]
  (> age 18))

(defn adult? [{:keys [age]}]
  (> age 18))

(defn language-fan? [{:keys [language]}]
  (= language "Clojure"))

(defn job-offer [{:keys [name]}]
  (str "Job offer for: " name " (imagine an email is sent now)."))

(defn emailer! [s]
  (str "Email is being sent: " s))

(defn acquire-candidate-by-id-maybe-hiring! [id]
  (some->> (get-users 2)
           (filter adult?)
           (filter language-fan?)
           (map job-offer)
           (map emailer!)
           ))

(defn process-file [filename f]
  (with-open [reader (clojure.java.io/reader filename)]
    (doseq [line (line-seq reader)]
      (-> line
          ;; You could use (read-string) IF you wanted side effects from data.
          ;; It will also error out on empty lines.  This format is better for
          ;; moving around objects over EDN.
          (clojure.edn/read-string)
          f))))

(defn edn-file-print []
  (process-file "test.edn" #(prn (:x %))))

;; Refer to it fully qualified
(s/valid? :blub.my-types/vendor {:vid "444412345678" :dba "My Business"}) ; true

;; Refer to it aliased
(s/valid? ::mt/vendor {:vid "444412345678" :dba "My Business"}) ; true

(defn hilo []
  (let [number (rand-int 10)]
    ((fn [times]
       (let [i (read)]
         (cond (= i number) (do (prn (format "You win in %s guessess!" times)) )
               (> i number) (do (prn "Lower") (recur (inc times)))
               (< i number) (do (prn "Higher") (recur (inc times)))))) 0)))

;; With transduce, they run the comps in natural order, fg = g(f)
(def xform
  (comp
   (filter odd?)
   (map #(* 2 %))))
(transduce xform conj (range 0 10)) ; 3 5 7 9 11

;; clojure.spec is a much better way to do this, but this will suffice for a simpler sample
(defn is-ip-format? [s] (re-find #"^\d+\.\d+\.\d+\.\d+" s))
(defn -assert [message] #(when (not %) (throw (Throwable. message))))
(def assert-ip-format (comp (-assert "Invalid IP format") is-ip-format?))

(defn get-ip []
  (slurp "http://httpbin.org/delay/10"))

(defn fake-response [] "{\"origin\": \"xxx\"}")

;; This could probably be a bit more readable with an actual promise or something.
(defn get-ip-with-timeout []
  (let [response (atom nil)]
    (future (Thread/sleep 1e3)
            (reset! response (fake-response)))
    (future  (reset! response (get-ip)))
    (while (= nil @response) (Thread/sleep 100))
    @response))

(defn make-ip-model [json ]
  (let [tmp (json/parse-string json)
        ip (get tmp "origin")]
    (assert-ip-format ip)
    {:ip ip}))

(def get-ip-model (comp make-ip-model get-ip-with-timeout))
;; (get-ip-model)

;; Server stuff
(defn recv
  "Show the received value."
  [x]
  ;; (prn (format "%02X or %X" x x))
  (prn (map int x))
  (prn (type x))
  (prn x)
  (println x))

;; Listen on an IP for udp
(defn start-udp [& r]
  (def socket (udp/create-udp-server 12346))

  ;; Just print the received messages
  (def my-future (udp/receive-loop socket (udp/empty-packet 512) #'recv)))

(defn stop-udp []
  (future-cancel my-future)
  (udp/close-udp-server socket))

(def restart-udp (comp start-udp stop-udp))

;; Client stuff
(defn send-udp [s]
  (let [socket (udp/create-udp-server 12347)
        packet (udp/packet (udp/get-bytes-utf8 s)
                           (udp/make-address "127.0.0.1")
                           12345)]
    (udp/send-message socket packet)
    (udp/close-udp-server socket)
    ))

;; Scenario:
;; You need to make 100 'API calls' and aggregate the data, with logging,
;; and the final evaluation result being the properly ordered return set.
(defn burner
  "Burn up the CPU for almost exactly 1 second."
  []
  (def looped (atom 0))
  (def start-time (System/nanoTime))
  (while (< (System/nanoTime) (+ start-time 1e9))
    (swap! looped inc))
  looped)

(def my-data-log (atom []))

(defn fetch-data! [n]
  ;; (Thread/sleep 1e3)
  (burner)
  (swap! my-data-log conj (str "Fetched record: " n)) n)

(defn get-data []
  (pmap fetch-data! (range 100)))

;; 4.x seconds
(defn concurrency-test []
  (time (doall (get-data)))
  nil)

;; This craziness is to read a file like page1.clj and eval it with string support
(import '[java.io PushbackReader])
(require '[clojure.java.io :as io])

;; https://stackoverflow.com/questions/24922478/is-there-a-way-to-read-all-the-forms-in-a-clojure-file
(defn read-all
  [file]
  (let [rdr (-> file io/file io/reader PushbackReader.)]
    (loop [forms []]
      (let [form (try (read rdr) (catch Exception e nil))]
        (if form
          (recur (conj forms form))
          forms)))))

(defn wrap-symbol [x]
  (if (list? x)
    (fn [] (with-out-str (eval x)))
    (fn [] (with-out-str (prn  x)))))

(defn clj->html [file]
  (def request {:name "Jon Smith"})
  (clojure.string/replace
   (->> (read-all file)
        (map wrap-symbol)
        (map eval)
        (map #(%))
        (clojure.string/join ""))
   #"\n" " "))

(defn foo [x]
  (def y x)
  (def y (* y y))
  (+ y x))

(def tester {:x true :y false :z true})

(filter #(% tester) (keys tester))

(defn to-string-helper [xs n res]
  (if (= 0 (count xs))
    res
    (recur (rest xs)
           (inc n)
           (str (first xs)
                (if (and (> n 0 )
                         (= 0 (mod n 3))) "," "") res))))

(defn number-to-array [n res]
  (if (= n 0)
    (if (empty? res) [0] res)
    (recur (quot n 10) (conj res (mod n 10)))))

(defn to-string [n]
  (str (if (> 0 n) "-" "")
       (to-string-helper
        (number-to-array (Math/abs n) []) 0 "")))
