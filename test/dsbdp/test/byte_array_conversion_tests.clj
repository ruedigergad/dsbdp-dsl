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
  dsbdp.test.byte-array-conversion-tests
  (:require [clojure.test :refer :all]
            [dsbdp.byte-array-conversion :refer :all]))

(deftest int4l-conversion-test
  (let [ba (byte-array (map byte [18]))
        expected 2]
    (is (= expected (int4l ba 0)))))

(deftest int4h-conversion-test
  (let [ba (byte-array (map byte [18]))
        expected 1]
    (is (= expected (int4h ba 0)))))

(deftest int8-conversion-test
  (let [ba (byte-array (map byte [42]))
        expected 42]
    (is (= expected (int8 ba 0)))))

(deftest int16-conversion-test
  (let [ba (byte-array (map byte [1 2]))
        expected 258]
    (is (= expected (int16 ba 0)))))

(deftest int16be-conversion-test
  (let [ba (byte-array (map byte [2 1]))
        expected 258]
    (is (= expected (int16be ba 0)))))

(deftest int32-conversion-test
  (let [ba (byte-array (map byte [1 2 3 4]))
        expected 16909060]
    (is (= expected (int32 ba 0)))))

(deftest int32be-conversion-test
  (let [ba (byte-array (map byte [4 3 2 1]))
        expected 16909060]
    (is (= expected (int32be ba 0)))))

(deftest timestamp-conversion-test
  (let [ba (byte-array (map byte [84 -57 -106 -5 0 14 -54 15]))
        expected 1422366459969231000]
    (is (= expected (timestamp ba 0)))))

(deftest timestamp-be-conversion-test
  (let [ba (byte-array (map byte [-5 -106 -57 84 15 -54 14 0 -5]))
        expected 1422366459969231000]
    (is (= expected (timestamp-be ba 0)))))

(deftest eth-mac-addr-conversion-test-1
  (let [ba (byte-array (map byte [1 2 3 4 5 6]))
        expected "01:02:03:04:05:06"]
  (is (= expected (eth-mac-addr ba 0)))))

(deftest eth-mac-addr-conversion-test-2
  (let [ba (byte-array (map byte [-1 -2 -3 -14 -15 -16]))
        expected "TODO"]
  (is (= expected (eth-mac-addr ba 0)))))

(deftest ipv4-addr-conversion-test-1
  (let [ba (byte-array (map byte [1 2 3 4]))
        expected "1.2.3.4"]
  (is (= expected (ipv4-addr ba 0)))))

(deftest ipv4-addr-conversion-test-2
  (let [ba (byte-array (map byte [-4 -3 -2 -1]))
        expected "TODO"]
  (is (= expected (ipv4-addr ba 0)))))

