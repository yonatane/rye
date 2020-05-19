(defproject rye "0.1.0-SNAPSHOT"
  :description "Misc error handling"
  :url "https://github.com/yonatane/rye"
  :license {:name "MIT License"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :profiles {:dev {:dependencies [[criterium "0.4.5"]]}}
  :global-vars {*warn-on-reflection* true}
  :pedantic? :abort
  :java-source-paths ["src/rye/jump"])
