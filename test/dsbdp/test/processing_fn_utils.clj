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

(deftest simple-combine-proc-fns-1st-test
  (let [proc-fns (create-proc-fn-vec-from-template '(fn [i _] (+ i :_idx_ 1)) '(fn [_ o] (+ o :_idx_ 1)) 6)
        combined-proc-fn (combine-proc-fns proc-fns 0 3)]
    (is (= 1 ((proc-fns 0) 0 nil)))
    (is (= 2 ((proc-fns 1) nil 0)))
    (is (= 6 ((proc-fns 5) nil 0)))
    (is (= 6 (combined-proc-fn 0 nil)))
    (is (= 10 (combined-proc-fn 4 nil)))))

(deftest simple-combine-proc-fns-nth-test
  (let [proc-fns (create-proc-fn-vec-from-template '(fn [i _] (+ i :_idx_ 1)) '(fn [_ o] (+ o :_idx_ 1)) 6)
        combined-proc-fn (combine-proc-fns proc-fns 3 5)]
    (is (= 4 ((proc-fns 3) nil 0)))
    (is (= 5 ((proc-fns 4) nil 0)))
    (is (= 9 (combined-proc-fn nil 0)))
    (is (= 15 (combined-proc-fn nil 6)))))

(deftest simple-combine-proc-fn-vec-test
  (let [proc-fns (create-proc-fn-vec-from-template '(fn [i _] (+ i :_idx_ 1)) '(fn [_ o] (+ o :_idx_ 1)) 6)
        combined-proc-fn-vec (combine-proc-fn-vec [3 2 1] proc-fns)]
    (is (= 1 ((proc-fns 0) 0 nil)))
    (is (= 2 ((proc-fns 1) nil 0)))
    (is (= 6 ((proc-fns 5) nil 0)))
    (is (= 6 ((combined-proc-fn-vec 0) 0 nil)))
    (is (= 9 ((combined-proc-fn-vec 1) nil 0)))
    (is (= 6 ((combined-proc-fn-vec 2) nil 0)))))

