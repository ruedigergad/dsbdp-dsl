(defproject dsbdp-dsl "0.5.1-SNAPSHOT"
;(defproject dsbdp "0.5.0"
  :description "Dynamic Stream and Batch Data Processing (dsbdp) - Domain Specific Language (DSL)"
  :url "https://github.com/ruedigergad/dsbdp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-assorted-utils "1.18.3"]]
  :global-vars {*warn-on-reflection* true}
  :java-source-paths ["src-java"]
;  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :profiles  {:repl  {:dependencies  [[jonase/eastwood "0.3.5" :exclusions  [org.clojure/clojure]]]}}
  :plugins [[lein-cloverage "1.0.6"]]
  :test2junit-output-dir "ghpages/test-results"
  :test2junit-run-ant true  
  :html5-docs-docs-dir "ghpages/doc"
  :html5-docs-ns-includes #"^dsbdp.*"
  :html5-docs-repository-url "https://github.com/ruedigergad/dsbdp/blob/master"
  :aot :all
  :main dsbdp.main)
