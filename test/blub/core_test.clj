(ns blub.core-test
  (:require [clojure.test :as t :refer :all]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [blub.core :refer :all]))

;; https://stackoverflow.com/questions/40697841/howto-include-clojure-specd-functions-in-a-test-suite
(defmacro defspec-test
  ([name sym-or-syms] `(defspec-test ~name ~sym-or-syms nil))
  ([name sym-or-syms opts]
   (when t/*load-tests*
     `(def ~(vary-meta name assoc
                       :test `(fn []
                                (let [check-results# (clojure.spec.test.alpha/check ~sym-or-syms ~opts)
                                      checks-passed?# (every? nil? (map :failure check-results#))]
                                  (if checks-passed?#
                                    (t/do-report {:type    :pass
                                                  :message (str "Generative tests pass for "
                                                                (str/join ", " (map :sym check-results#)))})
                                    (doseq [failed-check# (filter :failure check-results#)
                                            :let [r# (clojure.spec.test.alpha/abbrev-result failed-check#)
                                                  failure# (:failure r#)]]
                                      (t/do-report
                                       {:type     :fail
                                        :message  (with-out-str (clojure.spec.alpha/explain-out failure#))
                                        :expected (->> r# :spec rest (apply hash-map) :ret)
                                        :actual   (if (instance? Throwable failure#)
                                                    failure#
                                                    (:clojure.spec.test.alpha/val failure#))})))
                                  checks-passed?#)))
        (fn [] (t/test-var (var ~name)))))))


(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(defspec-test test-addition [blub.core/addition])
