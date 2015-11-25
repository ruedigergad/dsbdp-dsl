(defproject dsbdp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-assorted-utils "1.11.0"]]
  :global-vars {*warn-on-reflection* true}
  :java-source-paths ["src-java"]
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :main dsbdp.main)
