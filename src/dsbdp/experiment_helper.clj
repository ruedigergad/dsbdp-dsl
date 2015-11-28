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
    [dsbdp.byte-array-conversion :refer :all]))

(defmacro create-proc-fns
  [fn-1 fn-n n]
  (loop [fns (prewalk-replace {:idx 0} [fn-1])]
    (if (< (count fns) n)
      (recur (conj fns (prewalk-replace {:idx (count fns)} fn-n)))
      (do
        (println "proc-fns-full:" fns)
        fns))))

(defmacro create-no-op-proc-fns
  [n]
  `(create-proc-fns (fn [~'_ ~'_]) (fn [~'_ ~'_]) ~n))

(defmacro create-inc-proc-fns
  [n]
  `(create-proc-fns (fn [~'i ~'_] (inc ~'i)) (fn [~'_ ~'o] (inc ~'o)) ~n))

