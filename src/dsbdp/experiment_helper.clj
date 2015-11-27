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
  (:require [dsbdp.byte-array-conversion :refer :all]))

(defmacro create-proc-fns
  [fn-1 fn-n n]
  (loop [fns [fn-1]]
    (if (< (count fns) n)
      (recur (conj fns fn-n))
      (do
        (println "proc-fns:" fns)
        fns))))

