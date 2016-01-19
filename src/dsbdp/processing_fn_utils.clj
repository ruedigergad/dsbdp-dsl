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

(defn create-mapped-proc-fn
  [fn-vec start-idx end-idx]
  (if (= 0 start-idx)
    nil
    nil
    ))

