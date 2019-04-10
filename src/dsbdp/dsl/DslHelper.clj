;;;
;;;   Copyright 2017 - 2019, Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Java Interop for using the DSL from within Java."} 
  dsbdp.dsl.DslHelper
  (:import (java.io StringWriter))
  (:require [dsbdp.dsl.core :refer :all]
            [clojure.pprint :refer :all])
  (:gen-class
   :methods [^:static [generateProcessingFn [Object] clojure.lang.IFn]
             ^:static [prettyPrint [Object] String]]))

(defn -generateProcessingFn [dsl-expression]
  (if (string? dsl-expression)
    (create-proc-fn (binding [*read-eval* true] (read-string dsl-expression)))
    (create-proc-fn dsl-expression)))

(defn -prettyPrint [obj]
  (with-out-str (pprint obj)))

