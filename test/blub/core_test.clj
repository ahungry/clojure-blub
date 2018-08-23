(ns blub.core-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [blub.core :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(defn xtest-all []
  (println (stest/enumerate-namespace 'blub.core))
  (-> (stest/enumerate-namespace 'blub.core) stest/check))

(xtest-all)

(deftest blub-core-spec
  (testing "Spec"
    (stest/check `blub.core/addition)
    (xtest-all)))
