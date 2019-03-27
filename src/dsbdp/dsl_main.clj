;;;
;;;   Copyright 2019 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Main class for launching a test CLI."}
  dsbdp.dsl-main
  (:require
    (cli4clj [cli :refer :all])
    (clj-assorted-utils [util :refer :all])
    (dsbdp
      [data-processing-dsl :refer :all]
      [byte-array-conversion :refer :all]
      [experiment-helper :refer :all]))
  (:gen-class))

(defn -main [& args]
  (println "Starting dsbdp dsl main...")
  )

