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
  dsbdp.local-data-processing-pipeline
  (:require [dsbdp.data-processing-dsl :refer :all])
  (:import
    (dsbdp LocalTransferContainer ProcessingLoop)
    (java.util.concurrent ArrayBlockingQueue BlockingQueue LinkedTransferQueue)))

(def ^:dynamic *queue-size* 100000)

(defn create-local-processing-element
  [^BlockingQueue in-queue f]
  (let [out-queue (LinkedTransferQueue.)
        running (atom true)
        proc-loop (ProcessingLoop.
                    (fn []
                      (try
                        (let [^LocalTransferContainer c (.take in-queue)
                              new-out (f (.getIn c) (.getOut c))]
                          (if (not (nil? new-out))
                            (.setOut c new-out))
                          (.put out-queue c))
                        (catch InterruptedException e
                          (if @running
                            (throw e))))))]
    (.start proc-loop)
    {:interrupt (fn []
                  (reset! running false)
                  (.interrupt proc-loop))
     :get-out-queue out-queue}))

(defn interrupt
  [obj]
  ((obj :interrupt)))

(defn get-out-queue
  [obj]
  (obj :get-out-queue))

