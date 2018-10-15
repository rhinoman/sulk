(defproject sulk "0.1.0"
  :description "Software license key toolkit"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [cheshire "5.8.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.clojure/tools.cli "0.4.1"]]
  :main ^:skip-aot sulk.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :uberjar-name "sulk.jar"}
             :dev {:jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5010"]
                   :dependencies [[org.clojure/tools.nrepl "0.2.13"]]}
             }
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options ["-target" "1.8" "-source" "1.8"])
