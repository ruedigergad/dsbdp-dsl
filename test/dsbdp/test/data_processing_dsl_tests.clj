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
            [dsbdp.data-processing-dsl :refer :all]))

(def byte-array-test-data
  "We use the byte array representation of a captured UDP packet as byte array test data."
  (byte-array
    (map byte [-5 -106 -57 84   15 -54 14 0   77 0 0 0   77 0 0 0    ; 16 byte pcap header
               -1 -2 -3 -14 -15 -16 1 2 3 4 5 6 8 0                  ; 14 byte Ethernet header
               69 0 0 32 0 3 64 0 7 17 115 -57 1 2 3 4 -4 -3 -2 -1   ; 20 byte IP header
               8 0 16 0 0 4 -25 -26                                  ; 8 byte UDP header
               97 98 99 100])))                                      ; 4 byte data "abcd"

(deftest byte-array-to-java-map-test
  (let [expected {"udpSrc" 2048, "udpDst" 4096}
        dsl-expression {:input-type :byte-array
                        :output-type :java-map
                        :rules [['udpSrc '(int16 50)]
                                ['udpDst '(int16 52)]]}
        data-processing-fn (create-data-processing-fn dsl-expression)
        result (data-processing-fn byte-array-test-data 0)]
    (is (= java.util.HashMap (type result)))
    (is (= expected result))))

