;;;
;;;   Copyright 2015 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Pipeline for processing data"}
  dsbdp.data-processing-pipeline
  (:require [dsbdp.data-processing-dsl :refer :all])
  (:import
    (dsbdp LocalTransferContainer ProcessingLoop)
    (java.util.concurrent LinkedTransferQueue)))

(defn create-local-processing-element
  [^LinkedTransferQueue in-queue f]
  (let [out-queue (LinkedTransferQueue.)
        proc-loop (ProcessingLoop.
                    (fn []
                      (let [^LocalTransferContainer c (.take in-queue)
                            new-out (f (.getIn c) (.getOut c))]
                        (if (not (nil? new-out))
                          (.setOut c new-out))
                        (.offer out-queue c))))]
    (.start proc-loop)
    {:interrupt (fn []
                 (.interrupt proc-loop))}))

(defn interrupt
  [obj]
  ((obj :interrupt)))

