;;;
;;;   Copyright 2015 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "DSL for processing data"}
  dsbdp.data-processing-dsl
  (:require [dsbdp.byte-array-conversion :refer :all]))

(defn create-proc-sub-fn
  [data-processing-definition input]
  (into
    '()
    (reverse
      (reduce
        (fn [v data-proc-def-element]
          (cond
            (symbol? data-proc-def-element)
              (let [s data-proc-def-element]
                (cond
                  (or (= s 'nth) (= s 'get))
                    (conj v (ns-resolve 'clojure.core s) 'input)
                  (ns-resolve 'clojure.core s)
                    (conj v (ns-resolve 'clojure.core s))
                  (ns-resolve 'dsbdp.byte-array-conversion s)
                    (conj v (ns-resolve 'dsbdp.byte-array-conversion s) 'input)
                  :default
                    (do
                      (println "Could not resolve symbol:" s)
                      v)))
            (list? data-proc-def-element)
              (conj v (into '() (reverse (create-proc-sub-fn data-proc-def-element input))))
            :default (conj v data-proc-def-element)))
        [] data-processing-definition))))

;;; TODO: This function only supports byte array based functions yet.
;;; This will become problematic once other input data types,
;;; such as vectors or maps shall be processed to, e.g., CSV string format.
(defn get-proc-sub-fn-ret-type
  "Get the return type of a data processing sub-function data-proc-sub-fn.
   For determining the type, this function calls data-proc-sub-fn with a 1530 byte dummy byte-array filled with 0."
  [data-proc-sub-fn]
  (let [dummy-ba (byte-array 1530 (byte 0))
        ret (data-proc-sub-fn dummy-ba)]
    (type ret)))

(defn- create-proc-fn-body-java-map-out
  "Create a data processing function body for emitting data into a Java map."
  [input rules output]
  (reduce
    (fn [v rule]
      (conj v `(.put
                ~(name (first rule))
                ~(create-proc-sub-fn (second rule) input))))
    (if (nil? output)
      '[doto (java.util.HashMap.)]
      '[doto ^java.util.Map output])
    rules))

(defn- create-proc-fn-body-clj-map-out
  "Create a data processing function body for emitting data into a Clojure map."
  [input rules output]
  (reduce
    (fn [v rule]
      (conj v `(assoc
                ~(name (first rule))
                ~(create-proc-sub-fn (second rule) input))))
    (if (nil? output)
      '[-> {}]
      '[-> output])
    rules))

(defn- create-proc-fn-body-csv-str-out
  "Create a data processing function body for emitting data into a CSV string."
  [input rules]
  (let [extracted-strings (reduce
                            (fn [v rule]
                              (let [data-proc-sub-fn (create-proc-sub-fn (second rule) input)
                                    data-proc-sub-fn-ret-type (get-proc-sub-fn-ret-type (eval `(fn [~input] ~data-proc-sub-fn)))]
                                (conj v (if (= java.lang.String data-proc-sub-fn-ret-type)
                                          `(str "\"" ~data-proc-sub-fn "\"")
                                          data-proc-sub-fn))))
                            '[str] rules)
        commas (reduce into [] ["." (repeat (- (count rules) 1) ",") "."])]
    (vec (filter #(not= \. %) (interleave extracted-strings commas)))))

(defn- create-proc-fn-body-json-str-out
  "Create a data processing function body for emitting data into a JSON string."
  [input rules]
  (let [extracted-strings (conj
                            (reduce
                              (fn [v rule]
                                (let [data-proc-sub-fn (create-proc-sub-fn (second rule) input)
                                      data-proc-sub-fn-ret-type (get-proc-sub-fn-ret-type (eval `(fn [~input] ~data-proc-sub-fn)))]
                                  (conj v "\"" (name (first rule)) "\":"
                                          (if (= java.lang.String data-proc-sub-fn-ret-type)
                                            `(str "\"" ~data-proc-sub-fn "\"")
                                            data-proc-sub-fn))))
                              '[str "{"] rules)
                            "}")
        commas (reduce into [] ["." "." "." "." "." (reduce into [] (repeat (- (count rules) 1) ["," "." "." "."])) "." "."])]
;    (println (interleave extracted-strings commas))
    (vec (filter (fn [x] (and (not= \. x) (not= "." x))) (interleave extracted-strings commas)))))

(defn create-proc-fn
  "Create a data processing function based on the given dsl-expression."
  [dsl-expression]
;  (println "Got DSL expression:" dsl-expression)
  (let [input-sym 'input
        output-sym 'output
        output-type (name (:output-type dsl-expression))
        rules (:rules dsl-expression)
        fn-body-vec (condp = output-type
                      "java-map" (create-proc-fn-body-java-map-out input-sym rules nil)
                      "java-map-inc" (create-proc-fn-body-java-map-out input-sym rules output-sym)
                      "clj-map" (create-proc-fn-body-clj-map-out input-sym rules nil)
                      "clj-map-inc" (create-proc-fn-body-clj-map-out input-sym rules output-sym)
                      "csv-str" (create-proc-fn-body-csv-str-out input-sym rules)
                      "json-str" (create-proc-fn-body-json-str-out input-sym rules)
                      (do
                        (println "Unknown output type:" output-type)
                        (println "Defaulting to :java-map as output type.")
                        (create-proc-fn-body-java-map-out input-sym nil rules)))
;        _ (println "Created data processing function vector from DSL:" fn-body-vec)
        fn-body (reverse (into '() fn-body-vec))
;        _ (println "Created data processing function body:" fn-body)
        data-processing-fn (if (.endsWith output-type "-inc")
                             (eval `(fn [~input-sym ~output-sym] ~fn-body))
                             (eval `(fn [~input-sym] ~fn-body)))]
    data-processing-fn))

