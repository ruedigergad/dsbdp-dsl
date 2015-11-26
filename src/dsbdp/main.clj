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
        out-cntr (Counter.)
        delta-cntr (delta-counter)
        stats-fn (fn []
                   (println
                     "in:" (long (/ (delta-cntr :in (.value in-cntr)) 1000)) "k;"
                     "out:" (long (/ (delta-cntr :out (.value out-cntr)) 1000)) "k;"))
        out-fn (fn [_ _]
                 (.inc out-cntr))
        in-data 1
;        proc-fns [(fn [_ _])]
;        proc-fns [(fn [_ _])(fn [_ _])]
;        proc-fns [(fn [_ _])(fn [_ _])(fn [_ _])]
;        proc-fns [(fn [_ _])(fn [_ _])(fn [_ _])(fn [_ _])]
;        proc-fns [(fn [_ _])(fn [_ _])(fn [_ _])(fn [_ _])(fn [_ _])]
        proc-fns [(fn [_ _])(fn [_ _])(fn [_ _])(fn [_ _])(fn [_ _])(fn [_ _])]
;        proc-fns [(fn [_ _])(fn [_ _])(fn [_ _])(fn [_ _])(fn [_ _])(fn [_ _])(fn [_ _])]
;        proc-fns [(fn [i _] (inc i)) (fn [_ o] (inc o))]
        pipeline (create-local-processing-pipeline
                   proc-fns
                   out-fn)
        in-fn (get-in-fn pipeline)
        in-loop (ProcessingLoop.
                  (fn []
                    (in-fn in-data)
                    (.inc in-cntr)))]
    (.start in-loop)
    (run-repeat (executor) stats-fn 1000)))

