;;;
;;;   Copyright 2015 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Unit tests for the data processing pipeline"}
  dsbdp.test.data-processing-pipeline-tests
  (:require
    [clj-assorted-utils.util :refer :all]
    [clojure.test :refer :all]
    [dsbdp.data-processing-pipeline :refer :all])
  (:import
    (dsbdp Counter LocalTransferContainer)
    (java.util.concurrent LinkedTransferQueue)))

(deftest local-transfer-container-constructor-test
  (let [container (LocalTransferContainer. "foo" "bar")]
    (is (= "foo" (.getIn container)))
    (is (= "bar" (.getOut container)))))

(deftest local-transfer-container-set-out-test
  (let [container (LocalTransferContainer. "foo" "bar")]
    (is (= "bar" (.getOut container)))
    (.setOut container "baz")
    (is (= "baz" (.getOut container)))))

(deftest create-local-processing-element-test
  (let [in-queue (LinkedTransferQueue.)
        flag (prepare-flag)
        proc-element (create-local-processing-element in-queue
                                                      (fn [_ _] (set-flag flag)))]
    (.put in-queue (LocalTransferContainer. "in" "out"))
    (await-flag flag)
    (is (flag-set? flag))
    (interrupt proc-element)))

(deftest simple-local-processing-element-in-args-test
  (let [in-queue (LinkedTransferQueue.)
        flag (prepare-flag)
        in-arg (atom nil)
        out-arg (atom nil)
        proc-element (create-local-processing-element in-queue
                                                      (fn [in out]
                                                        (reset! in-arg in)
                                                        (reset! out-arg out)
                                                        (set-flag flag)))]
    (.put in-queue (LocalTransferContainer. "in" "out"))
    (await-flag flag)
    (is (= "in" @in-arg))
    (is (= "out" @out-arg))
    (interrupt proc-element)))

