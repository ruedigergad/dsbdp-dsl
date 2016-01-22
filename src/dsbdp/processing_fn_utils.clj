;;;
;;;   Copyright 2015 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Utility functions for working with processing fns."}
  dsbdp.processing-fn-utils
  (:require
    [clojure.walk :refer :all]
    [clojure.pprint :refer :all]))

(defn create-proc-fn-vec-from-template
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

(defn combine-proc-fns
  [fn-vec start-idx end-idx]
  (fn [in out]
    (loop [fns (subvec fn-vec start-idx end-idx) o out]
      (if (empty? fns)
        o
        (recur (rest fns) ((first fns) in o))))))

(defn combine-proc-fns-vec
  [fn-mapping fn-vec]
  (reduce
    (fn [v m]
      (let [start-idx (reduce + (subvec fn-mapping 0 (count v)))]
        (conj v (combine-proc-fns fn-vec start-idx (+ start-idx m)))))
    (let [f (combine-proc-fns fn-vec 0 (first fn-mapping))]
      [f])
    (rest fn-mapping)))

(defn calculate-distribution-mapping
  [in-seq ratios]
  (reduce
    (fn [v r]
      (conj v (int (* (count in-seq) r))))
    []
    ratios))

