(ns blub.my-types
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]))

(defn empty-string? [s] (= (count s) 0))
(defn not-empty-string? [s] (not (empty-string? s)))
(defn at-least-n-chars [n] #(>= (count %) n))

;; Define a unique type of ID
(def vid-regex #"^(4444|777).*")
(s/def ::vid (s/and string?
                    not-empty-string?
                    (at-least-n-chars 10)
                    #(re-matches vid-regex %)))

;; How about a pad function that only receives vids?
(defn pad-vid [vid]
  vid)

(s/fdef pad-vid
  :args (s/cat :vid ::vid)
  :ret ::vid)

(stest/instrument)

(s/def ::dba (s/and string? not-empty-string?))

(s/valid? ::vid "4444")                 ; false
(s/valid? ::vid "444412345678")         ; true

(s/def ::name (s/and string? not-empty-string?))
(s/def ::phone (s/and string? not-empty-string? (at-least-n-chars 10)))
(s/def ::email (s/and string? not-empty-string? (at-least-n-chars 6) #(re-matches #".*@.*" %)))

;; Define a map type
(s/def ::vendor (s/keys :req-un [::vid ::dba]))
(s/def ::contact (s/keys :req-un [::name ::phone]
                         :opt-un [::email]))

;; How about a union map type
(s/def ::vendor-with-contact-info (s/merge ::vendor ::contact))

;; Double colon references current ns
(s/valid? ::vendor {:vid "444412345678" :dba "My Business"}) ; true

;; Or you can explicitly reference it
(s/valid? :blub.my-types/vendor {:vid "444412345678" :dba ""}) ; false

;; Lets try the big type
(s/valid? ::vendor-with-contact-info {:vid "444412345678" :dba "My Business"}) ; false
(s/valid? ::vendor-with-contact-info {:vid "444412345678" :dba "My Business"
                                        :name "Matt" :phone "5553332222"}) ; true
(s/valid? ::vendor-with-contact-info
          {:vid "444412345678" :dba "My Business" :x 3 :email "oops"
           :name "Matt" :phone "5553332222"}) ; false, as email is invalid, but is optional
