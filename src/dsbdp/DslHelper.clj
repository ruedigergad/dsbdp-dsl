;;;
;;;   Copyright 2017, Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Java Interop for using the DSL from within Java."} 
  dsbdp.DslHelper
  (:require [dsbdp.data-processing-dsl :refer :all]) 
  (:gen-class
   :methods [^:static [generateProcessingFn [Object] clojure.lang.IFn]]))

(defn -generateProcessingFn [dsl-expression]
  (if (string? dsl-expression)
    (create-proc-fn (binding [*read-eval* true] (read-string dsl-expression)))
    (create-proc-fn dsl-expression)))

