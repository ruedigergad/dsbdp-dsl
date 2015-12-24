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
  dsbdp.test.local-data-processing-pipeline-tests
  (:require
    [clj-assorted-utils.util :refer :all]
    [clojure.test :refer :all]
    [dsbdp.local-data-processing-pipeline :refer :all])
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

(deftest create-local-processing-element-id-test
  (let [in-queue (LinkedTransferQueue.)
        proc-element (create-local-processing-element in-queue
                                                      (fn [_ _])
                                                      123)]
    (is (= 123 (get-id proc-element)))
    (is (= "ProcessingElement_123" (get-thread-name proc-element)))
    (interrupt proc-element)))

(deftest create-local-processing-element-no-id-test
  (let [in-queue (LinkedTransferQueue.)
        proc-element (create-local-processing-element in-queue
                                                      (fn [_ _]))]
    (is (nil? (get-id proc-element)))
    (is (nil? (get-thread-name proc-element)))
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

(deftest simple-local-processing-element-output-test
  (let [in-queue (LinkedTransferQueue.)
        flag (prepare-flag)
        proc-element (create-local-processing-element in-queue
                                                      (fn [in out]
                                                        (set-flag flag)
                                                        "foobar"))]
    (.put in-queue (LocalTransferContainer. "in" "out"))
    (await-flag flag)
    (let [out (.take (get-out-queue proc-element))]
      (is (= "in" (.getIn out)))
      (is (= "foobar" (.getOut out))))
    (interrupt proc-element)))

(deftest simple-local-processing-element-chaining-test
  (let [in-queue (LinkedTransferQueue.)
        flag (prepare-flag)
        proc-element-1 (create-local-processing-element in-queue
                                                        (fn [in out]
                                                          (str in "foo")))
        proc-element-2 (create-local-processing-element (get-out-queue proc-element-1)
                                                        (fn [in out]
                                                          (set-flag flag)
                                                          (str out "bar")))]
    (.put in-queue (LocalTransferContainer. "in" "out"))
    (await-flag flag)
    (let [out (.take (get-out-queue proc-element-2))]
      (is (= "in" (.getIn out)))
      (is (= "infoobar" (.getOut out))))
    (interrupt proc-element-1)
    (interrupt proc-element-2)))



(deftest simple-local-pipeline-send-test
  (let [flag-proc (prepare-flag)
        flag-out (prepare-flag)
        proc-fns [(fn [_ _] (set-flag flag-proc))]
        pipeline (create-local-processing-pipeline proc-fns (fn [_ _] (set-flag flag-out)))]
    ((get-in-fn pipeline) "foo")
    (await-flag flag-proc)
    (await-flag flag-out)
    (is (flag-set? flag-proc))
    (is (flag-set? flag-out))
    (interrupt pipeline)))

(deftest simple-three-staged-local-pipeline-send-test
  (let [flag-out (prepare-flag)
        out (atom nil)
        proc-fns [(fn [in _] (inc in)) (fn [_ out] (inc out)) (fn [_ out] (inc out))]
        pipeline (create-local-processing-pipeline proc-fns (fn [_ o] (reset! out o) (set-flag flag-out)))]
    ((get-in-fn pipeline) 0)
    (await-flag flag-out)
    (is (flag-set? flag-out))
    (is (= 3 @out))
    (interrupt pipeline)))

