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

(def ^:dynamic *incremental-indicator-suffix* "#inc")

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
  [input rules output]
  (reduce
    (fn [v rule]
      (let [data-proc-sub-fn (create-proc-sub-fn (second rule) input)
            tmp-v (if (some #{:string} rule)
                    (conj v `(.append "\"") `(.append ~data-proc-sub-fn) `(.append "\""))
                    (conj v `(.append ~data-proc-sub-fn)))]
        (if (not= rule (last rules))
          (conj tmp-v `(.append ","))
          tmp-v)))
    (if (nil? output)
      '[doto (java.lang.StringBuilder.)]
      '[doto ^java.lang.StringBuilder output])
    rules))

(defn- create-proc-fn-body-json-str-out
  "Create a data processing function body for emitting data into a JSON string."
  [input rules output]
  (reduce
    (fn [v rule]
      (let [data-proc-sub-fn (create-proc-sub-fn (second rule) input)
            tmp-k (conj v `(.append "\"") `(.append ~(name (first rule))) `(.append "\":"))
            tmp-v (if (some #{:string} rule)
                    (conj tmp-k `(.append "\"") `(.append ~data-proc-sub-fn) `(.append "\""))
                    (conj tmp-k `(.append ~data-proc-sub-fn)))]
        (if (not= rule (last rules))
          (conj tmp-v `(.append ","))
          (conj tmp-v `(.append "}")))))
    (if (nil? output)
      '[doto (java.lang.StringBuilder.) (.append "{")]
      '[doto ^java.lang.StringBuilder output (.deleteCharAt (- (.length ^java.lang.StringBuilder output) 1)) (.append ",")])
    rules))

(defn create-proc-fn
  "Create a data processing function based on the given dsl-expression."
  [dsl-expression]
;  (println "Got DSL expression:" dsl-expression)
  (let [input-sym 'input
        output-type (name (:output-type dsl-expression))
        rules (:rules dsl-expression)
        output-sym (if (.endsWith output-type *incremental-indicator-suffix*)
                     'output)
        fn-body-vec (condp (fn [^String v ^String s] (.startsWith s v)) output-type
                      "java-map" (create-proc-fn-body-java-map-out input-sym rules output-sym)
                      "clj-map" (create-proc-fn-body-clj-map-out input-sym rules output-sym)
                      "csv-str" (create-proc-fn-body-csv-str-out input-sym rules output-sym)
                      "json-str" (create-proc-fn-body-json-str-out input-sym rules output-sym)
                      (do
                        (println "Unknown output type:" output-type)
                        (println "Defaulting to :java-map as output type.")
                        (create-proc-fn-body-java-map-out input-sym nil rules)))
;        _ (println "Created data processing function vector from DSL:" fn-body-vec)
        fn-body (reverse (into '() fn-body-vec))
;        _ (println "Created data processing function body:" fn-body)
        data-processing-fn (if (not (nil? output-sym))
                             (eval `(fn [~input-sym ~output-sym] ~fn-body))
                             (eval `(fn [~input-sym] ~fn-body)))]
    data-processing-fn))

(defn create-partial-proc-fn
  [dsl-expression start-idx end-idx]
  (if (= 0 start-idx)
    (create-proc-fn
      {:output-type (:output-type dsl-expression)
       :rules (subvec (:rules dsl-expression) start-idx end-idx)})
    (create-proc-fn
      {:output-type (keyword (str (name (:output-type dsl-expression)) *incremental-indicator-suffix*))
       :rules (subvec (:rules dsl-expression) start-idx end-idx)})))

(defn create-proc-fns-vec
  [fn-mapping dsl-expression]
  (reduce
    (fn [v m]
      (let [start-idx (reduce + (subvec fn-mapping 0 (count v)))]
        (conj v (create-partial-proc-fn dsl-expression
                                        start-idx
                                        (+ start-idx m)))))
    (let [f (create-partial-proc-fn dsl-expression 0 (first fn-mapping))]
      [(fn [in _] (f in))])
    (rest fn-mapping)))

