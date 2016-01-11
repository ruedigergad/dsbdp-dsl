;;;
;;;   Copyright 2015 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Helper that are primarily used during experiments"}
  dsbdp.experiment-helper
  (:require
    [clojure.walk :refer :all]
    [clojure.pprint :refer :all]
    [dsbdp.byte-array-conversion :refer :all]
    [dsbdp.data-processing-dsl :refer :all]
    [opennlp.nlp :refer :all]
    [opennlp.treebank :refer :all]
    
    ) 
  (:import
    (java.util HashMap Map)
    (org.apache.commons.math3.util CombinatoricsUtils)))

(def pcap-byte-array-test-data
  "The byte array representation of a UDP packet for being used as dummy data."
  (byte-array
    (map byte [-5 -106 -57 84   15 -54 14 0   58 0 0 0   58 0 0 0                ; 16 byte pcap header
               -1 -2 -3 -14 -15 -16 1 2 3 4 5 6 8 0                              ; 14 byte Ethernet header
               69 0 0 44   0 3 64 0   7 17 115 -57   1 2 3 4   -4 -3 -2 -1       ; 20 byte IP header
               8 0 16 0 0 16 -25 -26                                              ; 8 byte UDP header
               97 98 99 100 101 102 103 104 105 106 107 108 109 110 111 112])))  ; 16 byte data "abcdefghijklmnop"

(def get-sentences (make-sentence-detector "resources/opennlp/models/en-sent.bin"))
(def tokenize (make-tokenizer "resources/opennlp/models/en-token.bin"))
(def pos-tag (make-pos-tagger "resources/opennlp/models/en-pos-maxent.bin"))
(def chunker (make-treebank-chunker "resources/opennlp/models/en-chunker.bin"))

(defn create-proc-fns
  [fn-1 fn-n n]
  (loop [fns (prewalk-replace {:_idx_ 0} [fn-1])]
    (if (< (count fns) n)
      (recur (conj fns (prewalk-replace {:_idx_ (count fns)} fn-n)))
      (do
        (println "proc-fns-full:" fns)
        (println "proc-fns-short:" (.replaceAll (str fns) "(?<=\\()([a-zA-Z\\.\\-]++/)" ""))
        (println "proc-fns-pretty:\n" (.replaceAll (str (with-out-str (pprint fns))) "(?<=\\()([a-zA-Z\\.\\-]++/)" ""))
        (vec
          (map eval fns))))))

(defn create-no-op-proc-fns
  [n]
  (create-proc-fns
    '(fn [_ _] 0)
    '(fn [_ _] 1)
    n))

(defn create-inc-proc-fns
  [n]
  (create-proc-fns
    '(fn [i _] (inc i))
    '(fn [_ o] (inc o))
    n))

(defn create-hashmap-inc-put-proc-fns
  [n]
  (let [o-sym 'o]
    (let [o-meta (vary-meta o-sym assoc :tag 'java.util.Map)]
     (create-proc-fns
       '(fn [i _] (doto (java.util.HashMap.) (.put (str :_idx_) (inc i))))
       '(fn [_ o-meta] (.put o-meta (str :_idx_) (inc (.get o-meta (str (dec :_idx_))))))
       n))))

(defn factorial
  ([n]
    (factorial 1N 1N n))
  ([result i n]
    (if (<= i n)
      (recur (* result i) (inc i) n)
      result)))

(defn create-factorial-proc-fns
  [n]
  (create-proc-fns
    '(fn [i _] (dsbdp.experiment-helper/factorial i))
    '(fn [i _] (dsbdp.experiment-helper/factorial i))
     n))

(def sample-pcap-processing-definition-rules
  [['timestamp '(timestamp-str-be 0) :string]
   ['capture-length '(int32be 8)]
   ['eth-src '(eth-mac-addr-str 22) :string]
   ['eth-dst '(eth-mac-addr-str 16) :string]
   ['ip-src '(ipv4-addr-str 42) :string]
   ['ip-dst '(ipv4-addr-str 46) :string]
   ['ip-ver '(int4h 30)]
   ['ip-length '(float (/ (int16 32) 65535))]
   ['ip-id '(float (/ (int16 34) 65535))]
   ['ip-ttl '(float (/ (int8 38) 255))]
   ['ip-protocol '(float (/ (int8 39) 255))]
   ['ip-checksum '(float (/ (int16 40) 65535))]
   ['udp-src '(float (/ (int16 50) 65535))]
   ['udp-dst '(float (/ (int16 52) 65535))]
   ['udp-length '(float (/ (int16 54) 65535))]
   ['udp-checksum '(float (/ (int16 56) 65535))]
   ['udp-payload '(ba-to-str 58 16) :string]])

(def sample-pcap-processing-definition-json
  {:output-type :json-str
   :rules sample-pcap-processing-definition-rules})

(def sample-pcap-processing-definition-clj-map
  {:output-type :clj-map
   :rules sample-pcap-processing-definition-rules})

(def sample-pcap-processing-definition-java-map
  {:output-type :java-map
   :rules sample-pcap-processing-definition-rules})

(defn opennlp-single-sentence-direct-test-fn
  [sentence]
  (phrases (chunker (pos-tag (tokenize sentence)))))

(defn opennlp-direct-test-fn
  [in-str]
  (doseq [sentence (get-sentences in-str)]
    (opennlp-single-sentence-direct-test-fn sentence)))

