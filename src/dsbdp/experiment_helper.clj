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
    [dsbdp.byte-array-conversion :refer :all])
  (:import
    (java.util HashMap Map)
    (org.apache.commons.math3.util CombinatoricsUtils)))

(def pcap-byte-array-test-data
  "The byte array representation of a UDP packet for being used as dummy data."
  (byte-array
    (map byte [-5 -106 -57 84   15 -54 14 0   77 0 0 0   77 0 0 0    ; 16 byte pcap header
               -1 -2 -3 -14 -15 -16 1 2 3 4 5 6 8 0                  ; 14 byte Ethernet header
               69 0 0 32 0 3 64 0 7 17 115 -57 1 2 3 4 -4 -3 -2 -1   ; 20 byte IP header
               8 0 16 0 0 4 -25 -26                                  ; 8 byte UDP header
               97 98 99 100])))                                      ; 4 byte data "abcd"

(defn create-proc-fns
  [fn-1 fn-n n]
  (loop [fns (prewalk-replace {:idx 0} [fn-1])]
    (if (< (count fns) n)
      (recur (conj fns (prewalk-replace {:idx (count fns)} fn-n)))
      (do
        (println "proc-fns-full:" fns)
        (println "proc-fns-short:" (.replaceAll (str fns) "(?<=\\()([a-zA-Z\\.\\-]++/)" ""))
        (println "proc-fns-pretty:\n" (.replaceAll (str (with-out-str (pprint fns))) "(?<=\\()([a-zA-Z\\.\\-]++/)" ""))
        (vec
          (map eval fns))))))

(defn create-no-op-proc-fns
  [n]
  (create-proc-fns
    '(fn [_ _])
    '(fn [_ _])
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
       '(fn [i _] (doto (java.util.HashMap.) (.put (str :idx) (inc i))))
       '(fn [_ o-meta] (.put o-meta (str :idx) (inc (.get o-meta (str (dec :idx))))))
       n))))

(defn factorial
  [n]
  (loop [result 1N i 1N]
    (if (<= i n)
      (recur (* result i) (inc i))
      result)))

(defn create-factorial-proc-fns
  [n]
  (create-proc-fns
    '(fn [i _] (dsbdp.experiment-helper/factorial i))
    '(fn [i _] (dsbdp.experiment-helper/factorial i))
     n))

