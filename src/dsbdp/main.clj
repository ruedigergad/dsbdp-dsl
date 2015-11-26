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
    (dsbdp Counter ProcessingLoop)
    (java.util HashMap Map))
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
        predefined-proc-fns {:no-op-1 [(fn [_ _])]
                             :no-op-2 [(fn [_ _]) (fn [_ _])]
                             :no-op-3 [(fn [_ _]) (fn [_ _]) (fn [_ _])]
                             :no-op-4 [(fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _])]
                             :no-op-5 [(fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _])]
                             :no-op-6 [(fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _])]
                             :no-op-7 [(fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _])]
                             :no-op-8 [(fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _]) (fn [_ _])]
                             :inc-1 [(fn [i _] (inc i))]
                             :inc-2 [(fn [i _] (inc i)) (fn [_ o] (inc o))]
                             :inc-3 [(fn [i _] (inc i)) (fn [_ o] (inc o)) (fn [_ o] (inc o))]
                             :inc-4 [(fn [i _] (inc i)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o))]
                             :inc-5 [(fn [i _] (inc i)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o))]
                             :inc-6 [(fn [i _] (inc i)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o))]
                             :inc-7 [(fn [i _] (inc i)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o))]
                             :inc-8 [(fn [i _] (inc i)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o)) (fn [_ o] (inc o))]
                             :map-put-1 [(fn [i _] (doto (HashMap.) (.put "1" i)))]
                             :map-put-2 [(fn [i _] (doto (HashMap.) (.put "1" i))) (fn [i ^Map o] (.put o "2" i))]
                             :map-put-3 [(fn [i _] (doto (HashMap.) (.put "1" i))) (fn [i ^Map o] (.put o "2" i)) (fn [i ^Map o] (.put o "3" i))]
                             :map-put-4 [(fn [i _] (doto (HashMap.) (.put "1" i))) (fn [i ^Map o] (.put o "2" i)) (fn [i ^Map o] (.put o "3" i)) (fn [i ^Map o] (.put o "4" i))]
                             :map-put-5 [(fn [i _] (doto (HashMap.) (.put "1" i))) (fn [i ^Map o] (.put o "2" i)) (fn [i ^Map o] (.put o "3" i)) (fn [i ^Map o] (.put o "4" i)) (fn [i ^Map o] (.put o "5" i))]
                             :map-put-6 [(fn [i _] (doto (HashMap.) (.put "1" i))) (fn [i ^Map o] (.put o "2" i)) (fn [i ^Map o] (.put o "3" i)) (fn [i ^Map o] (.put o "4" i)) (fn [i ^Map o] (.put o "5" i)) (fn [i ^Map o] (.put o "6" i))]
                             :map-put-7 [(fn [i _] (doto (HashMap.) (.put "1" i))) (fn [i ^Map o] (.put o "2" i)) (fn [i ^Map o] (.put o "3" i)) (fn [i ^Map o] (.put o "4" i)) (fn [i ^Map o] (.put o "5" i)) (fn [i ^Map o] (.put o "6" i)) (fn [i ^Map o] (.put o "7" i))]
                             :map-put-8 [(fn [i _] (doto (HashMap.) (.put "1" i))) (fn [i ^Map o] (.put o "2" i)) (fn [i ^Map o] (.put o "3" i)) (fn [i ^Map o] (.put o "4" i)) (fn [i ^Map o] (.put o "5" i)) (fn [i ^Map o] (.put o "6" i)) (fn [i ^Map o] (.put o "7" i)) (fn [i ^Map o] (.put o "8" i))]}
        pipeline (create-local-processing-pipeline
                   (predefined-proc-fns :map-put-5)
                   out-fn)
        in-fn (get-in-fn pipeline)
        in-loop (ProcessingLoop.
                  (fn []
                    (in-fn in-data)
                    (.inc in-cntr)))]
    (.start in-loop)
    (run-repeat (executor) stats-fn 1000)))

