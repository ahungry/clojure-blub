;; https://clojure.github.io/core.typed/#clojure.core.typed/List
;; https://github.com/gnl/ghostwheel#how-to-read-a-readme

(ns blub.core
  #:ghostwheel.core{:check true
                    :num-tests 10}
  ;; (:require [clojure.core.typed :as t])
  ;; (:require [clojure.core.typed :as t :refer [ann check-ns]])
  (:require [clojure.spec.alpha :as s])
  (:require [clojure.spec.gen.alpha :as gen])
  (:require [clojure.spec.test.alpha :as stest])
  (:require [ghostwheel.core :as g
             :refer [>defn >defn- >fdef => | <- ?]])
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
