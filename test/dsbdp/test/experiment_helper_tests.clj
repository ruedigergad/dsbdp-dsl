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
    [dsbdp.experiment-helper :refer :all]))

(deftest simple-create-proc-fns-test
  (let [proc-fns (create-proc-fns (fn [_ _] "1") (fn [_ _] "n") 4)]
    (is (= 4 (count proc-fns)))
    (is (= "1" ((first proc-fns) nil nil)))
    (is (= "n" ((nth proc-fns 1) nil nil)))
    (is (= "n" ((last proc-fns) nil nil)))))

