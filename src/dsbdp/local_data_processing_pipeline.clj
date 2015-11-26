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
    (java.util.concurrent ArrayBlockingQueue BlockingQueue LinkedTransferQueue TransferQueue)))



(def ^:dynamic *queue-size* 100000)



(defmacro create-queue
  []
  `(LinkedTransferQueue.))
;  `(ArrayBlockingQueue. *queue-size*))

(defn enqueue
  [^TransferQueue queue data]
;  [^BlockingQueue queue data]
;  (.offer queue data))
  (.tryTransfer queue data))



(defn create-local-processing-element
  [^BlockingQueue in-queue f]
  (let [running (atom true)
        out-queue (create-queue)
        proc-loop (ProcessingLoop.
                    (fn []
                      (try
                        (let [^LocalTransferContainer c (.take in-queue)
                              new-out (f (.getIn c) (.getOut c))]
                          (if (not (nil? new-out))
                            (.setOut c new-out))
                          (enqueue out-queue c))
                        (catch InterruptedException e
                          (if @running
                            (throw e))))))]
    (.start proc-loop)
    {:interrupt (fn []
                  (reset! running false)
                  (.interrupt proc-loop))
     :out-queue out-queue}))

(defn interrupt
  [obj]
  ((obj :interrupt)))

(defn get-out-queue
  [obj]
  (obj :out-queue))



(defn create-local-processing-pipeline
  [fns out-fn]
  (let [running (atom true)
        in-queue (create-queue)
        proc-elements (reduce
                        (fn [v f]
                          (conj v (create-local-processing-element (get-out-queue (last v)) f)))
                        [(create-local-processing-element in-queue (first fns))]
                        (rest fns))
        ^BlockingQueue out-queue (get-out-queue (last proc-elements))
        out-proc-loop (ProcessingLoop.
                        (fn []
                          (try
                            (let [^LocalTransferContainer c (.take out-queue)]
                              (if (not (nil? c))
                                (out-fn (.getIn c) (.getOut c))
                                (out-fn nil nil)))
                            (catch InterruptedException e
                              (if @running
                                (throw e))))))]
    (.start out-proc-loop)
    {:interrupt (fn []
                  (reset! running false)
                  (doseq [pe proc-elements]
                    (interrupt pe))
                  (.interrupt out-proc-loop))
     :in-fn (fn [in]
              (enqueue in-queue (LocalTransferContainer. in nil)))}))

(defn get-in-fn
  [obj]
  (obj :in-fn))

