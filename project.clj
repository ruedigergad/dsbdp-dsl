(defproject dsbdp "0.4.0-SNAPSHOT"
;(defproject dsbdp "0.3.0"
  :description "Dynamic Stream and Batch Data Processing (dsbdp)"
  :url "https://github.com/ruedigergad/dsbdp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"]
                 [clj-assorted-utils "1.17.1"]
                 [org.clojure/tools.cli "0.3.3"]]
  :global-vars {*warn-on-reflection* true}
  :java-source-paths ["src-java"]
;  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :profiles  {:repl  {:dependencies  [[jonase/eastwood "0.2.2" :exclusions  [org.clojure/clojure]]]}}
  :plugins [[lein-cloverage "1.0.6"]]
  :test2junit-output-dir "ghpages/test-results"
  :test2junit-run-ant true  
  :html5-docs-docs-dir "ghpages/doc"
  :html5-docs-ns-includes #"^dsbdp.*"
  :html5-docs-repository-url "https://github.com/ruedigergad/dsbdp/blob/master"
  :aot :all
  :main dsbdp.main)
