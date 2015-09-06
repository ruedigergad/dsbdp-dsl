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

(defn create-data-processing-sub-fn
  [data-processing-definition input offset]
  (into
    '()
    (reverse
      (reduce
        (fn [v data-proc-def-element]
          (cond
            (or
              (keyword? data-proc-def-element)
              (symbol? data-proc-def-element)) (let [s (symbol (name data-proc-def-element))]
                                                 (condp not= nil
                                                   (ns-resolve 'clojure.core s)
                                                     (conj v (ns-resolve 'clojure.core s))
                                                   (ns-resolve 'dsbdp.byte-array-conversion s)
                                                     (conj v (ns-resolve 'dsbdp.byte-array-conversion s) 'input)
                                                   (do
                                                     (println "Could not resolve keyword/symbol:" s)
                                                      v)))
            (list? data-proc-def-element) (conj v (into '() (reverse (create-data-processing-sub-fn data-proc-def-element input offset))))
            :default (conj v data-proc-def-element)))
        [] data-processing-definition))))

(defn create-data-processing-fn-body-for-java-map-type
  "Create a data processing function body for extracting data into a Java map."
  [input offset rules]
  (reduce
    (fn [v rule]
      (conj v `(.put
                ~(name (first rule))
                ~(create-data-processing-sub-fn (second rule) input offset))))
    '[doto (java.util.HashMap.)] rules))

(defn create-data-processing-fn
  "Create a data processing function based on the given dsl-expression."
  [dsl-expression]
;  (println "Got DSL expression:" dsl-expression)
  (let [input-sym 'input
        offset-sym 'offset
        fn-body-vec (let [rules (:rules dsl-expression)
                          input-type (:input-type dsl-expression)
                          output-type (:output-type dsl-expression)]
                      (condp = (name output-type)
                        "java-map" (create-data-processing-fn-body-for-java-map-type input-sym offset-sym rules)
;                        "clj-map" (create-data-processing-fn-body-for-clj-map-type input-sym offset-sym rules)
;                        "csv-str" (create-data-processing-fn-body-for-csv-str-type input-sym offset-sym rules)
;                        "json-str" (create-data-processing-fn-body-for-json-str-type input-sym offset-sym rules)
                        (do
                          (println "Unknown output type:" output-type)
                          (println "Defaulting to :java-maps as output type.")
                          (create-data-processing-fn-body-for-java-map-type input-sym offset-sym rules))))
;        _ (println "Created data processing function vector from DSL:" fn-body-vec)
        fn-body (reverse (into '() fn-body-vec))
;        _ (println "Created data processing function body:" fn-body)
        data-processing-fn (eval `(fn [~input-sym ~offset-sym] ~fn-body))]
    data-processing-fn))

