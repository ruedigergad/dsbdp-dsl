;;;
;;;   Copyright 2017 - 2019 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Unit tests for data processing DSL Java Interoperation"}
  dsbdp.dsl.test.core-java-interop-tests
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [dsbdp.dsl.experiment-helper :refer :all])
  (:import (dsbdp.dsl DslHelper)
           (java.util ArrayList HashMap List Map)))



(deftest simple-documenation-example-test-1
  (let [in-vector [1.23 "FOO" 42 "bar" "baz"]
        expected-clj-map {"myFloat" 1.23, "myStr" "foo", "myRatio" 0.42, "myStr2" "barbaz"}
        expected-json-str "{\"myFloat\":1.23,\"myStr\":\"foo\",\"myRatio\":0.42,\"myStr2\":\"barbaz\"}"
        expected-csv-str "1.23,\"foo\",0.42,\"barbaz\""
        dsl-expression {:output-type :csv-str
                        :rules [['myFloat '(nth 0)]
                                ['myStr '(clojure.string/lower-case (nth 1)) :string]
                                ['myRatio '(/ (nth 2) 100.0)]
                                ['myStr2 '(str (nth 3) (nth 4)) :string]]}
        data-proc-fn-csv-str (DslHelper/generateProcessingFn dsl-expression)
        data-proc-fn-json-str (DslHelper/generateProcessingFn (assoc dsl-expression :output-type :json-str))
        data-proc-fn-clj-map (DslHelper/generateProcessingFn (assoc dsl-expression :output-type :clj))
        data-proc-fn-java-map (DslHelper/generateProcessingFn (assoc dsl-expression :output-type :java))
        ]
    (is (= expected-csv-str (str (data-proc-fn-csv-str in-vector))))
    (is (= expected-json-str (str (data-proc-fn-json-str in-vector))))
    (is (= expected-clj-map (data-proc-fn-java-map in-vector)))
    (is (= expected-clj-map (data-proc-fn-clj-map in-vector)))
    (is (= expected-csv-str (str (.invoke data-proc-fn-csv-str in-vector))))
    (is (= expected-json-str (str (.invoke data-proc-fn-json-str in-vector))))
    (is (= expected-clj-map (.invoke data-proc-fn-java-map in-vector)))
    (is (= expected-clj-map (.invoke data-proc-fn-clj-map in-vector)))))

(deftest simple-documenation-example-test-2-from-string
  (let [in-vector [1.23 "FOO" 42 "bar" "baz"]
        expected-clj-map {"myFloat" 1.23, "myStr" "foo", "myRatio" 0.42, "myStr2" "barbaz"}
        expected-json-str "{\"myFloat\":1.23,\"myStr\":\"foo\",\"myRatio\":0.42,\"myStr2\":\"barbaz\"}"
        expected-csv-str "1.23,\"foo\",0.42,\"barbaz\""
        dsl-expression-str (str "{:output-type :csv-str "
                                " :rules [[myFloat (nth 0)] "
                                "         [myStr (clojure.string/lower-case (nth 1)) :string] "
                                "         [myRatio (/ (nth 2) 100.0)] "
                                "         [myStr2 (str (nth 3) (nth 4)) :string]]}")
        data-proc-fn-csv-str (DslHelper/generateProcessingFn dsl-expression-str)
        data-proc-fn-json-str (DslHelper/generateProcessingFn (string/replace dsl-expression-str ":csv-str" ":json-str"))
        data-proc-fn-clj-map (DslHelper/generateProcessingFn (string/replace dsl-expression-str ":csv-str" ":clj"))
        data-proc-fn-java-map (DslHelper/generateProcessingFn (string/replace dsl-expression-str ":csv-str" ":java"))
        ]
    (is (= expected-csv-str (str (data-proc-fn-csv-str in-vector))))
    (is (= expected-json-str (str (data-proc-fn-json-str in-vector))))
    (is (= expected-clj-map (data-proc-fn-java-map in-vector)))
    (is (= expected-clj-map (data-proc-fn-clj-map in-vector)))
    (is (= expected-csv-str (str (.invoke data-proc-fn-csv-str in-vector))))
    (is (= expected-json-str (str (.invoke data-proc-fn-json-str in-vector))))
    (is (= expected-clj-map (.invoke data-proc-fn-java-map in-vector)))
    (is (= expected-clj-map (.invoke data-proc-fn-clj-map in-vector)))))



;;;
;;; Tests for byte array input and various output types.
;;;
(deftest byte-array-to-java-map-test
  (let [expected {"udpSrc" 2048, "udpDst" 4096}
        dsl-expression-str (str "{:output-type :java "
                                " :rules [[udpSrc (int16 50)] "
                                "         [udpDst (int16 52)]]}")
        data-processing-fn (DslHelper/generateProcessingFn dsl-expression-str)
        result (.invoke data-processing-fn pcap-byte-array-test-data)]
    (is (instance? Map result))
    (is (= expected result))))

(deftest byte-array-to-clj-map-test
  (let [expected {"udpSrc" 2048, "udpDst" 4096}
        dsl-expression-str (str "{:output-type :clj "
                                " :rules [[udpSrc (int16 50)] "
                                "         [udpDst (int16 52)]]}")
        data-processing-fn (DslHelper/generateProcessingFn dsl-expression-str)
        result (.invoke data-processing-fn pcap-byte-array-test-data)]
    (is (map? result))
    (is (= expected result))))

(deftest byte-array-to-csv-str-test
  (let [expected "2048,4096"
        dsl-expression-str (str "{:output-type :csv-str "
                                " :rules [[udpSrc (int16 50)] "
                                "         [udpDst (int16 52)]]}")
        data-processing-fn (DslHelper/generateProcessingFn dsl-expression-str)
        result (.invoke data-processing-fn pcap-byte-array-test-data)]
    (is (= expected (str result)))))

(deftest byte-array-to-json-str-test
  (let [expected "{\"udpSrc\":2048,\"udpDst\":4096}"
        dsl-expression-str (str "{:output-type :json-str "
                                " :rules [[udpSrc (int16 50)] "
                                "         [udpDst (int16 52)]]}")
        data-processing-fn (DslHelper/generateProcessingFn dsl-expression-str)
        result (.invoke data-processing-fn pcap-byte-array-test-data)]
    (is (= expected (str result)))))



;;;;
;;;; Tests for other input data types.
;;;;
;(deftest clojure-vector-to-java-map-with-additional-operation-and-two-data-values-test
;  (let [expected {"quotient" 2}
;        input-data [1 8 1 1 1 4 1 1]
;        dsl-expression {:output-type :java
;                        :rules [['quotient '(/ (nth 1) (nth 5))]]}
;        data-processing-fn (create-proc-fn dsl-expression)
;        result (data-processing-fn input-data)]
;    (is (instance? Map result))
;    (is (= expected result))))
;
;(deftest clojure-list-to-java-map-with-additional-operation-and-two-data-values-test
;  (let [expected {"quotient" 2}
;        input-data '(1 8 1 1 1 4 1 1)
;        dsl-expression {:output-type :java
;                        :rules [['quotient '(/ (nth 1) (nth 5))]]}
;        data-processing-fn (create-proc-fn dsl-expression)
;        result (data-processing-fn input-data)]
;    (is (instance? Map result))
;    (is (= expected result))))
;
;(deftest java-list-to-java-map-with-additional-operation-and-two-data-values-test
;  (let [expected {"quotient" 2}
;        input-data (reduce (fn [l v] (.add ^List l v) l) (ArrayList.) [1 8 1 1 1 4 1 1])
;        dsl-expression {:output-type :java
;                        :rules [['quotient '(/ (nth 1) (nth 5))]]}
;        data-processing-fn (create-proc-fn dsl-expression)
;        result (data-processing-fn input-data)]
;    (is (instance? Map result))
;    (is (= expected result))))
;
;(deftest clojure-map-to-java-map-with-additional-operation-and-two-data-values-test
;  (let [expected {"quotient" 2}
;        input-data {"x" 12, "y" 42, :z 6}
;        dsl-expression {:output-type :java
;                        :rules [['quotient '(/ (get "x") (get :z))]]}
;        data-processing-fn (create-proc-fn dsl-expression)
;        result (data-processing-fn input-data)]
;    (is (instance? Map result))
;    (is (= expected result))))
;
;(deftest java-map-to-java-map-with-additional-operation-and-two-data-values-test
;  (let [expected {"quotient" 2}
;        input-data (doto (HashMap.) (.put "x" 12) (.put "y" 42) (.put :z 6))
;        dsl-expression {:output-type :java
;                        :rules [['quotient '(/ (get "x") (get :z))]]}
;        data-processing-fn (create-proc-fn dsl-expression)
;        result (data-processing-fn input-data)]
;    (is (instance? Map result))
;    (is (= expected result))))

(deftest admnet-2017-dsl-pcap-file-test
  (let [expected {"dst" "FF:FE:FD:F2:F1:F0",
                  "data" {"len" 249,
                          "dst" "127.0.0.1",
                          "data" {"dst" 61481,
                                  "src" 55522,
                                  "flags" #{"PSH" "ACK"},
                                  "flags-value" 24,
                                  }
                          "src" "127.0.0.1",
                          "protocol-id" 6}
                  "src" "01:02:03:04:05:06",
                  "off" 14}
        dsl-expression-str (str
                            "{:output-type :java-map"
                            " :rules [[off (cond"
                            "			      (= 2 (int32be 0)) 4"
                            "				  (and"
                            "					(or"
                            "					  (= 0 (int16 0))"
                            "					  (= 4 (int16 0)))"
                            "					(= 1 (int16 2))"
                            "					(= 0x800 (int16 14))) 16"
                            "				  :default 14)]"
                            "		   [dst (eth-mac-addr-str 0)]"
                            "		   [src (eth-mac-addr-str 6)]"
                            "		   [data [[len (int16 (+ off 2))]"
                            "				  [src (ipv4-addr-str (+ off 12))]"
                            "				  [dst (ipv4-addr-str (+ off 16))]"
                            "				  [protocol-id (int8 (+ off 9))]"
                            "				  [data [(= 17 __1_protocol-id)"
                            "						   [[src (int16 (+ off 20))]"
                            "							[dst (int16 (+ off 22))]]"
                            "						 (= 6 __1_protocol-id)"
                            "						   [[src (int16 (+ off 20))]"
                            "							[dst (int16 (+ off 22))]"
                            "							[flags-value (int8 (+ off 33))]"
                            "							[flags (reduce-kv"
                            "									 #=(eval"
                            "										 `(fn [r# k# v#]"
                            "											(cond"
                            "											  (> (bit-and ~'__2_flags-value (bit-shift-left 1 k#)) 0)"
                            "												(conj r# v#)"
                            "											  :default r#)))"
                            "									 #{}"
                            "									 [\"FIN\" \"SYN\" \"RST\" \"PSH\" \"ACK\" \"URG\" \"ECE\" \"CWR\"])]]"
                            "						 (= 1 __1_protocol-id)"
                            "						   [[type (condp = (int8 (+ off 20))"
                            "									0 \"Echo Reply\""
                            "									3 \"Destination Unreachable\""
                            "									8 \"Echo Request\""
                            "								   (str \"Unknown ICMP Type:\" (int8 (+ off 20))))]"
                            "							[seq-no (int16 (+ off 26))]]]]]]]}")
        data-processing-fn (DslHelper/generateProcessingFn dsl-expression-str)
        result (data-processing-fn pcap-tcp-byte-array-test-data-without-pcap-header)]
    (is (instance? java.util.Map result))
    (is (= expected result))))

(deftest admnet-2017-dsl-pcap-file-with-offsets-test
  (let [expected {"dst" "FF:FE:FD:F2:F1:F0",
                  "dst__offset" 0,
                  "data" {"protocol-id__offset" 23,
                          "len" 249,
                          "dst" "127.0.0.1",
                          "dst__offset" 30,
                          "data" {"dst" 61481,
                                  "dst__offset" 36,
                                  "src" 55522,
                                  "flags" #{"PSH" "ACK"},
                                  "src__offset" 34,
                                  "flags-value" 24,
                                  "flags-value__offset" 47},
                          "src" "127.0.0.1",
                          "protocol-id" 6,
                          "src__offset" 26,
                          "len__offset" 16},
                  "src" "01:02:03:04:05:06",
                  "src__offset" 6,
                  "off" 14}
        dsl-expression-str (str
                            "{:output-type :java-map"
                            " :with-offsets :inline"
                            " :rules [[off (cond"
                            "			      (= 2 (int32be 0)) 4"
                            "				  (and"
                            "					(or"
                            "					  (= 0 (int16 0))"
                            "					  (= 4 (int16 0)))"
                            "					(= 1 (int16 2))"
                            "					(= 0x800 (int16 14))) 16"
                            "				  :default 14)]"
                            "		   [dst (eth-mac-addr-str 0)]"
                            "		   [src (eth-mac-addr-str 6)]"
                            "		   [data [[len (int16 (+ off 2))]"
                            "				  [src (ipv4-addr-str (+ off 12))]"
                            "				  [dst (ipv4-addr-str (+ off 16))]"
                            "				  [protocol-id (int8 (+ off 9))]"
                            "				  [data [(= 17 __1_protocol-id)"
                            "						   [[src (int16 (+ off 20))]"
                            "							[dst (int16 (+ off 22))]]"
                            "						 (= 6 __1_protocol-id)"
                            "						   [[src (int16 (+ off 20))]"
                            "							[dst (int16 (+ off 22))]"
                            "							[flags-value (int8 (+ off 33))]"
                            "							[flags (reduce-kv"
                            "									 #=(eval"
                            "										 `(fn [r# k# v#]"
                            "											(cond"
                            "											  (> (bit-and ~'__2_flags-value (bit-shift-left 1 k#)) 0)"
                            "												(conj r# v#)"
                            "											  :default r#)))"
                            "									 #{}"
                            "									 [\"FIN\" \"SYN\" \"RST\" \"PSH\" \"ACK\" \"URG\" \"ECE\" \"CWR\"])]]"
                            "						 (= 1 __1_protocol-id)"
                            "						   [[type (condp = (int8 (+ off 20))"
                            "									0 \"Echo Reply\""
                            "									3 \"Destination Unreachable\""
                            "									8 \"Echo Request\""
                            "								   (str \"Unknown ICMP Type:\" (int8 (+ off 20))))]"
                            "							[seq-no (int16 (+ off 26))]]]]]]]}")
        data-processing-fn (DslHelper/generateProcessingFn dsl-expression-str)
        result (data-processing-fn pcap-tcp-byte-array-test-data-without-pcap-header)]
    (is (instance? java.util.Map result))
    (is (= expected result))))

