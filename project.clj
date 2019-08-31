(defproject blub "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[io.aviso/pretty "0.1.37"]]
  :middleware [io.aviso.lein-pretty/inject]
  :dependencies [
                 ;; Language related
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "0.4.500"]
                 [clojure.java-time "0.3.2"]

                 ;; Type related
                 [gnl/ghostwheel "0.2.3"]

                 ;; Util related
                 [clj-http "3.7.0"]
                 [cheshire "5.9.0"]
                 [slingshot "0.12.2"]

                 ;;  Network related
                 [udp-wrapper "0.1.1"]

                 ;; readability things
                 [io.aviso/pretty "0.1.37"]
                 [expound "0.7.2"]

                 ;; Logging related
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.clojure/tools.logging "0.5.0"]

                 ;; Probably not even needed
                 [nrepl "0.4.5"]
                 [clojure-lanterna "0.9.7"]
                 ]
  :injections []
  :main ^:skip-aot blub.core
  :target-path "target/%s"
  :profiles
  {
   :dev {:dependencies [[org.clojure/test.check "0.9.0"]]}
   :uberjar {:aot :all}})
