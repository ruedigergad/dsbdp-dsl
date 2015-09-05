;;;
;;;   Copyright 2015 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Unit tests for byte array conversion"}
  dsbdp.test.byte-array-conversion
  (:require [clojure.test :refer :all]
            [dsbdp.byte-array-conversion :refer :all]))

(deftest timestamp-conversion-test
  (let [ba (byte-array (map byte [84 -57 -106 -5 0 14 -54 15]))
        expected 1422366459969231000]
    (is (= expected (timestamp ba 0)))))

(deftest timestamp-be-conversion-test
  (let [ba (byte-array (map byte [-5 -106 -57 84 15 -54 14 0 -5]))
        expected 1422366459969231000]
    (is (= expected (timestamp ba 0)))))

