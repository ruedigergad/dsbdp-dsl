;;;
;;;   Copyright 2015 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Unit tests for utility functions for easing the handling of processing fns."}
  dsbdp.test.processing-fn-utils
  (:require
    [clojure.test :refer :all]
    [dsbdp.processing-fn-utils :refer :all]))

(deftest simple-create-proc-fn-vec-from-template-test
  (let [proc-fns (create-proc-fn-vec-from-template '(fn [_ _] "1") '(fn [_ _] "n") 4)]
    (is (= 4 (count proc-fns)))
    (is (= "1" ((first proc-fns) nil nil)))
    (is (= "n" ((nth proc-fns 1) nil nil)))
    (is (= "n" ((last proc-fns) nil nil)))))

(deftest create-proc-fn-vec-from-template-with-index-test
  (let [proc-fns (create-proc-fn-vec-from-template '(fn [_ _] :_idx_) '(fn [_ _] :_idx_) 4)]
    (is (= 4 (count proc-fns)))
    (is (= 0 ((first proc-fns) nil nil)))
    (is (= 1 ((nth proc-fns 1) nil nil)))
    (is (= 3 ((last proc-fns) nil nil)))))

(deftest create-proc-fn-vec-from-template-with-ret-val-test
  (let [proc-fns (create-proc-fn-vec-from-template '(fn [i _] (inc i)) '(fn [_ o] (inc o)) 4)]
    (is (= 4 (count proc-fns)))
    (is (= 1 ((first proc-fns) 0 nil)))
    (is (= 1 ((nth proc-fns 1) nil 0)))
    (is (= 1 ((last proc-fns) nil 0)))
    (is (= 4 (->>
               ((proc-fns 0) 0 nil)
               ((proc-fns 1) nil)
               ((proc-fns 2) nil)
               ((proc-fns 3) nil))))))

(deftest simple-create-mapped-proc-fn-test-1st
  (let [proc-fns (create-proc-fn-vec-from-template '(fn [i _] (+ i :_idx_ 1)) '(fn [_ o] (+ o :_idx_ 1)) 6)
        mapped-proc-fn (create-mapped-proc-fn proc-fns 0 3)]
    (is (= 1 ((proc-fns 0) 0 nil)))
    (is (= 2 ((proc-fns 1) nil 0)))
    (is (= 6 ((proc-fns 5) nil 0)))
    (is (= 6 (mapped-proc-fn 0 nil)))
    (is (= 10 (mapped-proc-fn 4 nil)))
    )
  )

