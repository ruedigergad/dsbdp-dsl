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

