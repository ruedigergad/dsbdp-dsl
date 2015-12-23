;;;
;;;   Copyright 2015 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Unit tests for data processing DSL"}
  dsbdp.test.data-processing-dsl-tests
  (:require [clojure.test :refer :all]
            [dsbdp.data-processing-dsl :refer :all])
  (:import (java.util ArrayList HashMap List Map)))

(def byte-array-test-data
  "We use the byte array representation of a captured UDP packet as byte array test data."
  (byte-array
    (map byte [-5 -106 -57 84   15 -54 14 0   77 0 0 0   77 0 0 0    ; 16 byte pcap header
               -1 -2 -3 -14 -15 -16 1 2 3 4 5 6 8 0                  ; 14 byte Ethernet header
               69 0 0 32 0 3 64 0 7 17 115 -57 1 2 3 4 -4 -3 -2 -1   ; 20 byte IP header
               8 0 16 0 0 4 -25 -26                                  ; 8 byte UDP header
               97 98 99 100])))                                      ; 4 byte data "abcd"



;;;
;;; Tests for byte array input and various output types.
;;;
(deftest byte-array-to-java-map-test
  (let [expected {"udpSrc" 2048, "udpDst" 4096}
        dsl-expression {:output-type :java-map
                        :rules [['udpSrc '(int16 50)]
                                ['udpDst '(int16 52)]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn byte-array-test-data)]
    (is (instance? Map result))
    (is (= expected result))))

(deftest byte-array-to-clj-map-test
  (let [expected {"udpSrc" 2048, "udpDst" 4096}
        dsl-expression {:output-type :clj-map
                        :rules [['udpSrc '(int16 50)]
                                ['udpDst '(int16 52)]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn byte-array-test-data)]
    (is (map? result))
    (is (= expected result))))

(deftest byte-array-to-csv-str-test
  (let [expected "2048,4096"
        dsl-expression {:output-type :csv-str
                        :rules [['udpSrc '(int16 50)]
                                ['udpDst '(int16 52)]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn byte-array-test-data)]
    (is (= expected (str result)))))

(deftest byte-array-to-json-str-test
  (let [expected "{\"udpSrc\":2048,\"udpDst\":4096}"
        dsl-expression {:output-type :json-str
                        :rules [['udpSrc '(int16 50)]
                                ['udpDst '(int16 52)]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn byte-array-test-data)]
    (is (= expected (str result)))))

(deftest byte-array-to-csv-str-qm-test
  (let [expected "\"2048\",4096"
        dsl-expression {:output-type :csv-str
                        :rules [['udpSrc '(int16 50) :qm]
                                ['udpDst '(int16 52)]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn byte-array-test-data)]
    (is (= expected (str  result)))))

(deftest byte-array-to-json-str-qm-test
  (let [expected "{\"udpSrc\":2048,\"udpDst\":\"4096\"}"
        dsl-expression {:output-type :json-str
                        :rules [['udpSrc '(int16 50)]
                                ['udpDst '(int16 52) :qm]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn byte-array-test-data)]
    (is (= expected (str result)))))



;;;
;;; Tests for incremental data processing.
;;;
(deftest incremental-byte-array-to-java-map-test
  (let [expected {"udpSrc" 2048, "udpDst" 4096, "foo" :bar}
        dsl-expression {:output-type :java-map#inc
                        :rules [['udpSrc '(int16 50)]
                                ['udpDst '(int16 52)]]}
        data-processing-fn (create-proc-fn dsl-expression)
        input  (HashMap. {"foo" :bar})
        result (data-processing-fn byte-array-test-data input)]
    (is (instance? Map result))
    (is (identical? input result))
    (is (= expected result))))

(deftest incremental-byte-array-to-clj-map-test
  (let [expected {"udpSrc" 2048, "udpDst" 4096, "foo" :bar}
        dsl-expression {:output-type :clj-map#inc
                        :rules [['udpSrc '(int16 50)]
                                ['udpDst '(int16 52)]]}
        data-processing-fn (create-proc-fn dsl-expression)
        input  {"foo" :bar}
        result (data-processing-fn byte-array-test-data input)]
    (is (map? result))
    (is (not (identical? input result)))
    (is (= expected result))))

(deftest incremental-byte-array-to-csv-str-test
  (let [expected "foo,bar,2048,4096"
        dsl-expression {:output-type :csv-str#inc
                        :rules [['udpSrc '(int16 50)]
                                ['udpDst '(int16 52)]]}
        data-processing-fn (create-proc-fn dsl-expression)
        input (doto (java.lang.StringBuilder.) (.append "foo,bar,"))
        result (data-processing-fn byte-array-test-data input)]
    (is (= expected (str result)))))

(deftest incremental-byte-array-to-json-str-test
  (let [expected "{\"foo\":\"bar\",\"udpSrc\":2048,\"udpDst\":4096}"
        dsl-expression {:output-type :json-str#inc
                        :rules [['udpSrc '(int16 50)]
                                ['udpDst '(int16 52)]]}
        data-processing-fn (create-proc-fn dsl-expression)
        input (doto (java.lang.StringBuilder.) (.append "{\"foo\":\"bar\"}"))
        result (data-processing-fn byte-array-test-data input)]
    (is (= expected (str result)))))



;;;
;;; Tests for more complex data processing definitions/functions.
;;;
(deftest byte-array-to-java-map-with-additional-operation-test
  (let [expected {"udpSrc" 1024, "udpDst" 2048}
        dsl-expression {:output-type :java-map
                        :rules [['udpSrc '(/ (int16 50) 2)]
                                ['udpDst '(/ (int16 52) 2)]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn byte-array-test-data)]
    (is (instance? Map result))
    (is (= expected result))))

(deftest byte-array-to-java-map-with-additional-operation-and-float-type-test
  (let [expected {"udpSrc" (float 0.031250477), "udpDst" (float 0.06250095)}
        dsl-expression {:output-type :java-map
                        :rules [['udpSrc '(float (/ (int16 50) 65535))]
                                ['udpDst '(float (/ (int16 52) 65535))]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn byte-array-test-data)]
    (is (instance? Map result))
    (is (= expected result))))

(deftest byte-array-to-java-map-with-additional-operation-and-two-data-values-test
  (let [expected {"quotient" 2}
        dsl-expression {:output-type :java-map
                        :rules [['quotient '(/ (int16 52) (int16 50))]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn byte-array-test-data)]
    (is (instance? Map result))
    (is (= expected result))))



;;;
;;; Tests for other input data types.
;;;
(deftest clojure-vector-to-java-map-with-additional-operation-and-two-data-values-test
  (let [expected {"quotient" 2}
        input-data [1 8 1 1 1 4 1 1]
        dsl-expression {:output-type :java-map
                        :rules [['quotient '(/ (nth 1) (nth 5))]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn input-data)]
    (is (instance? Map result))
    (is (= expected result))))

(deftest clojure-list-to-java-map-with-additional-operation-and-two-data-values-test
  (let [expected {"quotient" 2}
        input-data '(1 8 1 1 1 4 1 1)
        dsl-expression {:output-type :java-map
                        :rules [['quotient '(/ (nth 1) (nth 5))]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn input-data)]
    (is (instance? Map result))
    (is (= expected result))))

(deftest java-list-to-java-map-with-additional-operation-and-two-data-values-test
  (let [expected {"quotient" 2}
        input-data (reduce (fn [l v] (.add ^List l v) l) (ArrayList.) [1 8 1 1 1 4 1 1])
        dsl-expression {:output-type :java-map
                        :rules [['quotient '(/ (nth 1) (nth 5))]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn input-data)]
    (is (instance? Map result))
    (is (= expected result))))

(deftest clojure-map-to-java-map-with-additional-operation-and-two-data-values-test
  (let [expected {"quotient" 2}
        input-data {"x" 12, "y" 42, :z 6}
        dsl-expression {:output-type :java-map
                        :rules [['quotient '(/ (get "x") (get :z))]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn input-data)]
    (is (instance? Map result))
    (is (= expected result))))

(deftest java-map-to-java-map-with-additional-operation-and-two-data-values-test
  (let [expected {"quotient" 2}
        input-data (doto (HashMap.) (.put "x" 12) (.put "y" 42) (.put :z 6))
        dsl-expression {:output-type :java-map
                        :rules [['quotient '(/ (get "x") (get :z))]]}
        data-processing-fn (create-proc-fn dsl-expression)
        result (data-processing-fn input-data)]
    (is (instance? Map result))
    (is (= expected result))))

