;;;
;;;   Copyright 2015 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Main class for launching experiments"}    
  dsbdp.main
  (:use
    clj-assorted-utils.util
    dsbdp.data-processing-dsl
    dsbdp.byte-array-conversion
    dsbdp.local-data-processing-pipeline)
  (:import
    (dsbdp Counter ProcessingLoop))
  (:gen-class))

(defn -main [& args]
  (println "Starting dsbdp main...")
  (let [in-cntr (Counter.)
        delta-cntr (delta-counter)
        stats-fn (fn []
                   (println
                     "in:" (long (/ (delta-cntr :in (.value in-cntr)) 1000)) "k;"))
        in-loop (ProcessingLoop. (fn [] (.inc in-cntr)))]
    (.start in-loop)
    (run-repeat (executor) stats-fn 1000)))

