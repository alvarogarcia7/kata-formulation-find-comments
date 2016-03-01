(defproject find-comments "0.0.1-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.3"]]
  :main find-comments.core
  :aot [find-comments.core]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:plugins      [[lein-midje "3.1.3"]]
                   :dependencies [[midje "1.6.3"]]}}
  )
