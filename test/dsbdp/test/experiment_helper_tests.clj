;;;
;;;   Copyright 2015 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Unit tests for a helpers that are primarily used for experiments"}
  dsbdp.test.experiment-helper-tests
  (:require
    [clojure.test :refer :all]
    [dsbdp.experiment-helper :refer :all])
  (:import
    (java.util HashMap Map)))

(deftest simple-create-proc-fns-test
  (let [proc-fns (create-proc-fns (fn [_ _] "1") (fn [_ _] "n") 4)]
    (is (= 4 (count proc-fns)))
    (is (= "1" ((first proc-fns) nil nil)))
    (is (= "n" ((nth proc-fns 1) nil nil)))
    (is (= "n" ((last proc-fns) nil nil)))))

(deftest create-no-op-proc-fns-test
  (let [proc-fns (create-no-op-proc-fns 4)]
    (is (= 4 (count proc-fns)))
    (is (= nil ((first proc-fns) nil nil)))
    (is (= nil ((nth proc-fns 1) nil nil)))
    (is (= nil ((last proc-fns) nil nil)))))

(deftest create-inc-proc-fns-test
  (let [proc-fns (create-inc-proc-fns 4)]
    (is (= 4 (count proc-fns)))
    (is (= 2 ((first proc-fns) 1 nil)))
    (is (= 2 ((nth proc-fns 1) nil 1)))
    (is (= 2 ((last proc-fns) nil 1)))))

(deftest create-idx-proc-fns-test
  (let [proc-fns (create-proc-fns (fn [_ _] :idx) (fn [_ _] :idx) 4)]
    (is (= 4 (count proc-fns)))
    (is (= 0 ((first proc-fns) nil nil)))
    (is (= 1 ((nth proc-fns 1) nil nil)))
    (is (= 3 ((last proc-fns) nil nil)))))

(deftest create-hashmap-inc-put-proc-fns-test
  (let [proc-fns (create-hashmap-inc-put-proc-fns 4)]
    (is (= 4 (count proc-fns)))
    (let [m ((first proc-fns) 0 nil)]
      (is (= HashMap (type m)))
      (is (= 1 (.get m "0")))
      ((nth proc-fns 1) nil m)
      ((nth proc-fns 2) nil m)
      ((nth proc-fns 3) nil m)
      (is (= 2 (.get m "1")))
      (is (= 3 (.get m "2")))
      (is (= 4 (.get m "3"))))))

(deftest factorial-test
  (is (= 1 (factorial 1)))
  (is (= 2 (factorial 2)))
  (is (= 6 (factorial 3)))
  (is (= 24 (factorial 4))))

(deftest create-factorial-proc-fns-test
  (let [proc-fns (create-factorial-proc-fns 4)]
    (is (= 4 (count proc-fns)))
    (is (= 6 ((first proc-fns) 3 nil)))
    (is (= 6 ((nth proc-fns 1) 3 nil)))
    (is (= 6 ((last proc-fns) 3 nil)))))

