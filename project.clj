;(defproject dsbdp-dsl "0.7.1-SNAPSHOT"
(defproject dsbdp-dsl "0.7.0"
  :description "Dynamic Stream and Batch Data Processing (dsbdp) - Domain Specific Language (DSL)"
  :url "https://github.com/ruedigergad/dsbdp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [cli4clj "1.7.2"]
                 [clj-assorted-utils "1.18.3"]
                 [javax.xml.bind/jaxb-api "2.4.0-b180830.0359"]
                 [com.sun.xml.bind/jaxb-core "2.3.0.1"]
                 [com.sun.xml.bind/jaxb-impl "2.3.2"]]
  :global-vars {*warn-on-reflection* true}
  :java-source-paths ["src-java"]
;  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :profiles {:repl  {:dependencies  [[jonase/eastwood "0.3.5" :exclusions  [org.clojure/clojure]]]}}
  :plugins [[lein-cloverage "1.1.1"] [lein-html5-docs "3.0.3"]]
  :test2junit-output-dir "docs/test-results"
  :test2junit-run-ant true  
  :html5-docs-docs-dir "docs/doc"
  :html5-docs-ns-includes #"^dsbdp.*"
  :html5-docs-repository-url "https://github.com/ruedigergad/dsbdp-dsl/blob/master"
  :aot :all
  :main dsbdp.dsl.main
  )
