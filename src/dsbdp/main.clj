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
  (:require
    (clj-assorted-utils [util :refer :all])
    (dsbdp
      [data-processing-dsl :refer :all]
      [byte-array-conversion :refer :all]
      [experiment-helper :refer :all]
      [local-data-processing-pipeline :refer :all]))
  (:import
    (dsbdp Counter ProcessingLoop)
    (java.lang.management ManagementFactory ThreadInfo ThreadMXBean)
    (java.util HashMap Map))
  (:gen-class))

(defn create-thread-info-fn
  []
  (let [^ThreadMXBean tmxb (ManagementFactory/getThreadMXBean)
        cpu-time-supported (.isThreadCpuTimeSupported tmxb)
        delta-cntr (delta-counter)]
    (.setThreadContentionMonitoringEnabled tmxb true)
    (if cpu-time-supported
      (.setThreadCpuTimeEnabled tmxb true))
    (fn []
      (let [t-ids (sort (vec (.getAllThreadIds tmxb)))]
        (doseq [t-id t-ids]
          (let [^ThreadInfo t-info (.getThreadInfo tmxb ^long t-id)
                t-name (.getThreadName t-info)
                cpu-time (if cpu-time-supported
                           (double (/ (.getThreadCpuTime tmxb t-id) 1000000000.0))
                           -1)
                user-time (if cpu-time-supported
                            (double (/ (.getThreadUserTime tmxb t-id) 1000000000.0))
                            -1)
                waited (.getWaitedTime t-info)
                blocked (.getBlockedTime t-info)]
            (println (str t-id "," t-name "," cpu-time "," user-time "," waited "," blocked ","
                       (delta-cntr (str "cpu-" t-id) cpu-time) ","
                       (delta-cntr (str "user-" t-id) user-time) ","
                       (delta-cntr (str "waited-" t-id) waited) ","
                       (delta-cntr (str "blocked-" t-id) blocked)))))))))

(defn -main [& args]
  (println "Starting dsbdp main...")
  (.setName (Thread/currentThread) "Main")
  (let [in-cntr (Counter.)
        out-cntr (Counter.)
        no-op false
        delta-cntr (delta-counter)
        stats-fn (fn []
                   (let [in (double (/ (.value in-cntr) 1000.0))
                         out (double (/ (.value out-cntr) 1000.0))]
                     (println
                       "time-delta:" (delta-cntr :time (System/currentTimeMillis)) "ms;"
                       "in:" in "k;"
                       "out:" out "k;"
                       "in-delta:" (delta-cntr :in in) "k/s;"
                       "out-delta:" (delta-cntr :out out) "k/s;")))
        out-fn (fn [_ _]
                 (.inc out-cntr))
;        in-data 24N
        in-data (if (not no-op)
                  1)
        pipeline (if (not no-op)
                   (create-local-processing-pipeline
                     (create-no-op-proc-fns 2)
;                     (create-factorial-proc-fns 2)
                     out-fn))
        in-fn (if (not (nil? pipeline))
                (get-in-fn pipeline))
        in-loop (ProcessingLoop.
                  "InLoop"
                  (if (not (nil? in-fn))
                    (fn []
                      (in-fn in-data)
                      (.inc in-cntr))
                    (fn []
                      (.inc in-cntr))))
        thread-info-fn (create-thread-info-fn)]
    (.start in-loop)
    (run-repeat (executor) (fn []
                             (stats-fn)
                             (thread-info-fn) (println)
                             )
                1000)
    (run-once (executor) (fn [] (System/exit 0)) 120000)))

