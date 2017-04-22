(defproject html-to-ect-divider "0.1.0-SNAPSHOT"
  :description "CLI tool to divide marked html files to ECT template parts"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]]
  :main ^:skip-aot html-to-ect-divider.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
