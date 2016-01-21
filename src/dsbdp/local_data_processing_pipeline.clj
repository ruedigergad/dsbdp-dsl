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
  (:require [clj-assorted-utils.util :refer :all])
  (:import
    (dsbdp Counter LocalTransferContainer ProcessingLoop)
    (java.util.concurrent ArrayBlockingQueue BlockingQueue LinkedBlockingQueue LinkedTransferQueue TimeUnit TransferQueue)
    (uk.co.real_logic.queues OneToOneConcurrentArrayQueue3)))



(def ^Integer queue-size (int 512))
(def queue-setup
  (let [file-name "queue-setup.cfg"
        setup (if (file-exists? file-name)
                (do
                  (println "Using queue-setup defined in:" file-name)
                  (.trim ^String (slurp file-name)))
                (do
                  (println file-name "not found. Using default.")
;                  "ArrayBlockingQueue_put"))]
;                  "ArrayBlockingQueue_put-counted"))]
;                  "ArrayBlockingQueue_put-counted-yield"))]
;                  "ArrayBlockingQueue_add-counted-yield"))]
;                  "ArrayBlockingQueue_add-counted-yield_remove-yield"))]
;                  "ArrayBlockingQueue_add-counted_remove"))]
;                  "LinkedBlockingQueue_add-counted_remove"))]
;                  "OneToOneConcurrentArrayQueue3_add-counted-yield_remove-yield"))]
;                  "OneToOneConcurrentArrayQueue3_add-counted_remove-yield"))]
;                  "OneToOneConcurrentArrayQueue3_add-counted_remove"))]
;                  "ArrayBlockingQueue_offer-counted"))]
;                  "ArrayBlockingQueue_offer-counted-yield"))]
;                  "LinkedTransferQueue_transfer"))]
;                  "LinkedTransferQueue_tryTransfer-counted-no-timeout"))]
;                  "LinkedTransferQueue_transfer-counted-yield"))]
                  "LinkedTransferQueue_tryTransfer-counted-10ms"))]
    (println "Using queue-setup:" setup)
    setup))

(defmacro create-queue
  []
  (println "Setting up queue creation for:" queue-setup)
  (let [expr (condp (fn [^String v ^String s] (.startsWith s v)) queue-setup
               "ArrayBlockingQueue" `(ArrayBlockingQueue. queue-size)
               "LinkedBlockingQueue" `(LinkedBlockingQueue. queue-size)
               "LinkedTransferQueue" `(LinkedTransferQueue.)
               "OneToOneConcurrentArrayQueue3" `(OneToOneConcurrentArrayQueue3. queue-size)
               )]
    (println expr)
    expr))

(defmacro enqueue
  [queue data enqueued-counter dropped-counter]
  (println "Enqueueing data via:")
  (let [expr (condp (fn [^String v ^String s] (.contains s v)) queue-setup
               "add-counted-yield" `(if (< (.size ~queue) queue-size)
                                      (do
                                        (.add ~queue ~data)
                                        (.inc ~enqueued-counter))
                                      (do
                                        (.inc ~dropped-counter)
                                        (Thread/yield)))
               "add-counted" `(if (< (.size ~queue) queue-size)
                                (do
                                  (.add ~queue ~data)
                                  (.inc ~enqueued-counter))
                                (.inc ~dropped-counter))
               "add" `(if (< (.size ~queue) queue-size)
                        (.add ~queue ~data))
               "put-counted-yield" `(if (< (.size ~queue) queue-size)
                                      (do
                                        (.put ~queue ~data)
                                        (.inc ~enqueued-counter))
                                      (do
                                        (.inc ~dropped-counter)
                                        (Thread/yield)))
               "put-counted" `(if (< (.size ~queue) queue-size)
                                (do
                                  (.put ~queue ~data)
                                  (.inc ~enqueued-counter))
                                (.inc ~dropped-counter))
               "put" `(.put ~queue ~data)
               "offer-counted-yield" `(if (< (.size ~queue) queue-size)
                                        (do
                                          (.offer ~queue ~data)
                                          (.inc ~enqueued-counter))
                                        (do
                                          (.inc ~dropped-counter)
                                          (Thread/yield)))
               "offer-counted" `(if (.offer ~queue ~data)
                                  (.inc ~enqueued-counter)
                                  (.inc ~dropped-counter))
               "offer" `(.offer ~queue ~data)
               "transfer-counted-no-sleep" `(if (.hasWaitingConsumer ~queue)
                                              (do
                                                (.transfer ~queue ~data)
                                                (.inc ~enqueued-counter))
                                              (.inc ~dropped-counter))
               "transfer-counted-sleep-1ms" `(if (.hasWaitingConsumer ~queue)
                                               (do
                                                 (.transfer ~queue ~data)
                                                 (.inc ~enqueued-counter))
                                               (do
                                                 (.inc ~dropped-counter)
                                                 (sleep 1)))
               "transfer-counted-yield" `(if (.hasWaitingConsumer ~queue)
                                               (do
                                                 (.transfer ~queue ~data)
                                                 (.inc ~enqueued-counter))
                                               (do
                                                 (.inc ~dropped-counter)
                                                 (Thread/yield)))
               "transfer" `(.transfer ~queue ~data)
               "tryTransfer-counted-no-timeout" `(if (.tryTransfer ~queue ~data)
                                                   (.inc ~enqueued-counter)
                                                   (.inc ~dropped-counter))
               "tryTransfer-counted-1ms" `(if (.tryTransfer ~queue ~data 1 TimeUnit/MILLISECONDS)
                                            (.inc ~enqueued-counter)
                                            (.inc ~dropped-counter))
               "tryTransfer-counted-10ms" `(if (.tryTransfer ~queue ~data 10 TimeUnit/MILLISECONDS)
                                             (.inc ~enqueued-counter)
                                             (.inc ~dropped-counter))
               "tryTransfer-counted-100ms" `(if (.tryTransfer ~queue ~data 100 TimeUnit/MILLISECONDS)
                                              (.inc ~enqueued-counter)
                                              (.inc ~dropped-counter))
               "tryTransfer" `(.tryTransfer ~queue ~data))]
    (println expr)
    expr))

(defmacro take-from-queue
  [queue]
  (println "Retrieving data from queue via:")
  (let [expr (condp (fn [^String v ^String s] (.endsWith s v)) queue-setup
               "remove-yield" `(if (not (.isEmpty ~queue))
                                 (.remove ~queue)
                                 (Thread/yield))
               "remove" `(if (not (.isEmpty ~queue))
                           (.remove ~queue))
               `(.take ~queue))]
    (println expr)
    expr))



(defn create-local-processing-element
  ([^BlockingQueue in-queue initial-proc-fn]
    (create-local-processing-element in-queue initial-proc-fn nil))
  ([^BlockingQueue in-queue initial-proc-fn id]
    (let [running (atom true)
          out-queue (create-queue)
          out-counter (Counter.)
          out-drop-counter (Counter.)
          proc-fn (atom initial-proc-fn)
          handler-fn (fn []
                       (try
                         (let [^LocalTransferContainer c (take-from-queue in-queue)
                               new-out (if (not (nil? c))
                                         (@proc-fn (.getIn c) (.getOut c)))]
                           (when (not (nil? new-out))
                             (.setOut c new-out)
                             (enqueue out-queue c out-counter out-drop-counter)))
                         (catch InterruptedException e
                           (if @running
                             (throw e)))))
          thread-name (if (not (nil? id))
                        (str "ProcessingElement_" id))
          proc-loop (if (not (nil? thread-name))
                      (ProcessingLoop.
                        thread-name
                        handler-fn)
                      (ProcessingLoop.
                        handler-fn))]
      (.start proc-loop)
      {:get-counts-fn (fn []
                        {:out (.value out-counter)
                         :dropped (.value out-drop-counter)})
       :interrupt (fn []
                    (reset! running false)
                    (.interrupt proc-loop))
       :out-queue out-queue
       :id id
       :set-proc-fn (fn [new-proc-fn]
                      (reset! proc-fn new-proc-fn))
       :thread-name thread-name})))

(defn interrupt
  [obj]
  ((obj :interrupt)))

(defn get-out-queue
  [obj]
  (obj :out-queue))

(defn get-id
  [obj]
  (obj :id))

(defn get-thread-name
  [obj]
  (obj :thread-name))

(defn get-counts
  [obj]
  ((obj :get-counts-fn)))

(defn set-proc-fn
  [obj proc-fn]
  ((obj :set-proc-fn) proc-fn))

(defn create-local-processing-pipeline
  [fns out-fn]
  (let [running (atom true)
        in-queue (create-queue)
        proc-elements (atom
                        (reduce
                          (fn [v f]
                            (conj v (create-local-processing-element (get-out-queue (last v)) f (count v))))
                          [(create-local-processing-element in-queue (first fns) 0)]
                          (rest fns)))
        out-queue (atom (get-out-queue (last @proc-elements)))
        create-out-proc-loop #(ProcessingLoop.
                                "PipelineOut"
                                (fn []
                                  (try
                                    (let [^LocalTransferContainer c (take-from-queue ^BlockingQueue @out-queue)]
                                      (if (not (nil? c))
                                        (out-fn (.getIn c) (.getOut c))))
                                    (catch InterruptedException e
                                      (if @running
                                        (throw e))))))
        out-proc-loop (atom (create-out-proc-loop))
        in-counter (Counter.)
        in-drop-counter (Counter.)]
    (.start @out-proc-loop)
    {:get-counts-fn (fn []
                      (reduce
                        (fn [m pe]
                          (assoc m (get-id pe) (get-counts pe)))
                        {:pipeline {:in (.value in-counter)
                                    :dropped (.value in-drop-counter)}}
                        @proc-elements))
     :interrupt (fn []
                  (reset! running false)
                  (doseq [pe @proc-elements]
                    (interrupt pe))
                  (.interrupt @out-proc-loop))
     :in-fn (fn [in]
              (enqueue in-queue (LocalTransferContainer. in nil) in-counter in-drop-counter))
     :set-proc-fns-vec (fn [new-proc-fns-vec]
                         (when (< (count @proc-elements) (count new-proc-fns-vec))
                           (reset! running false)
                           (.interrupt @out-proc-loop)
                           (swap! proc-elements (fn [v]
                                                 (conj
                                                   v
                                                   (create-local-processing-element
                                                     (get-out-queue (last v))
                                                     (last new-proc-fns-vec)
                                                     (count v)))))
                           (reset! out-queue (get-out-queue (last @proc-elements)))
                           (reset! out-proc-loop (doto (create-out-proc-loop) (.start)))
                           (reset! running true))
                         (when (> (count @proc-elements) (count new-proc-fns-vec))
                           (interrupt (last @proc-elements))
                           (swap! proc-elements pop)
                           (reset! out-queue (get-out-queue (last @proc-elements)))
                           (reset! running false)
                           (.interrupt @out-proc-loop)
                           (reset! out-proc-loop (doto (create-out-proc-loop) (.start)))
                           (reset! running true))
                         (doall
                           (map
                             (fn [pe f]
                               (set-proc-fn pe f))
                             @proc-elements
                             new-proc-fns-vec)))}))

(defn get-in-fn
  [obj]
  (obj :in-fn))

(defn set-proc-fns-vec
  [obj fns]
  ((obj :set-proc-fns-vec) fns))

