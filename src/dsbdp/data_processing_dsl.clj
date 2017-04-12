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
  (:require [dsbdp.byte-array-conversion :refer :all]
            [clojure.pprint :refer :all]))

(def ^:dynamic *incremental-indicator-suffix* "#inc")

(defn- create-proc-sub-fn
  "Create a sub part of a processing function.
   The data-processing-definition will be processed recursively.
   This function is responsible for actually resolving the given data processing functions.
   The input symbol will be placed as first argument on the innermost terms."
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
                      (println "Warning: Could not resolve symbol:" s)
                      (println "Assuming" s "is intended as \"self-reference\".")
                      (conj v s))))
            (list? data-proc-def-element)
              (conj v (into '() (reverse (create-proc-sub-fn data-proc-def-element input))))
            :default (conj v data-proc-def-element)))
        [] data-processing-definition))))

(declare create-let-expression)

(defn- create-cond-expression
  [input rules body-creation-fn output nesting-level]
  (
    )
  )

(defn- create-bindings-vector
  [input rules body-creation-fn output nesting-level]
  (reduce
    (fn [v rule]
      (let [rule-name (first rule)
            rule-expression (second rule)]
        (cond
          (list? rule-expression) (conj v
                                    (if (> 1 nesting-level)
                                      (first rule)
                                      (symbol (str "__" nesting-level "_" rule-name)))
                                    (create-proc-sub-fn rule-expression input))
          (and
            (vector? rule-expression)
            (every? vector? rule-expression)) (do
                                                ;(println "Binding: Got a VECTOR..." (first rule) (second rule))
                                                (into
                                                  (conj v
                                                        rule-name
                                                        nil)
                                                  (create-bindings-vector
                                                    input
                                                    rule-expression
                                                    body-creation-fn
                                                    output
                                                    (inc nesting-level))))
          (and
            (vector? rule-expression)
            (every? list? (butlast (take-nth 2 rule-expression)))
            (every?
              vector?
                (butlast
                  (take-nth
                    2
                    (rest rule-expression))))) (do
                                                 (println "FOOOOOOOOOOOOOOOO")
                                                 (pprint rule-expression)
                                                 (println "")
                                                 (let [x (reduce
                                                           (fn [vect v]
                                                             (cond
                                                               (or (list? v)
                                                                   (keyword? v)) (conj vect v)
                                                               (vector? v) (conj vect
                                                                                 (create-let-expression input v body-creation-fn output (inc nesting-level)))
                                                               :default (do
                                                                          (println "Unknown rule expression part:" v)
                                                                          vect)))
                                                           ['clojure.core/cond]
                                                           rule-expression)]
                                                   (println "BAAAAARRRRRRRRRRRR")
                                                   (pprint x)
                                                   (println "BLAH")
                                                   (conj v rule-name (into '() (reverse x)))))
          :default (println "Binding: unknown element for rule:" (str rule)))))
    []
    rules))

(defn- create-let-expression
  [input rules body-creation-fn output nesting-level]
  `(let
    ~(create-bindings-vector input rules body-creation-fn output nesting-level)
    ~(reverse (into '() (body-creation-fn rules output nesting-level)))))

(defn- create-let-body-vec-java-map-out
  [rules output nesting-level]
  (reduce
    (fn [v rule]
      (cond
        (list? (second rule)) (conj v
                                    `(.put
                                      ~(name (first rule))
                                      ~(if (> 1 nesting-level)
                                         (first rule)
                                         (symbol (str "__" nesting-level "_" (first rule))))))
        (and
          (vector? (second rule))
          (every? vector? (second rule))) (do
                                            ;(println "Java Map Body: Got a Vector..." (first rule) (second rule))
                                            (conj v
                                                  `(.put
                                                     ~(name (first rule))
                                                     ~(reverse (into '() (create-let-body-vec-java-map-out (second rule) nil (inc nesting-level)))))))
        :default (println "Java Map Body: unknown element for rule:" (str rule))))
    (if (nil? output)
      '[doto (java.util.HashMap.)]
      '[doto ^java.util.Map output])
    rules))

(defn- create-let-body-vec-clj-map-out
  [rules output nesting-level]
  (reduce
    (fn [v rule]
      (cond
        (list? (second rule)) (conj v
                                    `(assoc
                                      ~(name (first rule))
                                      ~(if (> 1 nesting-level)
                                         (first rule)
                                         (symbol (str "__" nesting-level "_" (first rule))))))
        (and
          (vector? (second rule))
          (every? vector? (second rule))) (do
                                            ;(println "Java Map Body: Got a Vector..." (first rule) (second rule))
                                            (conj v
                                                  `(assoc
                                                     ~(name (first rule))
                                                     ~(reverse (into '() (create-let-body-vec-clj-map-out (second rule) nil (inc nesting-level)))))))
        :default (println "Clj Map Body: unknown element for rule:" (str rule))))
    (if (nil? output)
      '[-> {}]
      '[-> output])
    rules))

(defn- create-let-body-vec-csv-str-out
  [rules output nesting-level]
  (reduce
    (fn [v rule]
      (let [tmp-v (if (some #{:string} rule)
                    (conj v `(.append "\"") `(.append ~(first rule)) `(.append "\""))
                    (conj v `(.append ~(first rule))))]
        (if (not= rule (last rules))
          (conj tmp-v `(.append ","))
          tmp-v)))
    (if (nil? output)
      '[doto (java.lang.StringBuilder.)]
      '[doto ^java.lang.StringBuilder output])
    rules))

(defn- create-let-body-vec-json-str-out
  [rules output nesting-level]
  (reduce
    (fn [v rule]
      (let [tmp-k (conj v `(.append "\"") `(.append ~(name (first rule))) `(.append "\":"))
            tmp-v (if (some #{:string} rule)
                    (conj tmp-k `(.append "\"") `(.append ~(first rule)) `(.append "\""))
                    (conj tmp-k `(.append ~(first rule))))]
        (if (not= rule (last rules))
          (conj tmp-v `(.append ","))
          (conj tmp-v `(.append "}")))))
    (if (nil? output)
      '[doto (java.lang.StringBuilder.) (.append "{")]
      '[doto ^java.lang.StringBuilder output (.deleteCharAt (- (.length ^java.lang.StringBuilder output) 1)) (.append ",")])
    rules))

(defn create-proc-fn
  "Create a data processing function based on the given dsl-expression.
   An example of a dsl-expression for processing a Clojure seq is given below:
  
   {:output-type :clj-map
    :rules [['myFloat '(nth 0)]
            ['myStr '(clojure.string/lower-case (nth 1)) :string]
            ['myRatio '(/ (nth 2) 100.0)]
            ['myStr2 '(str (nth 3) (nth 4)) :string]]}
   
   The resulting function will for the input [1.23 \"FOO\" 42 \"bar\" \"baz\"] produce the output {\"myFloat\" 1.23, \"myStr\" \"foo\", \"myRatio\" 0.42, \"myStr2\" \"barbaz\"}."
  [dsl-expression]
;  (println "Got DSL expression:" dsl-expression)
  (let [input-sym 'input
        output-type (name (:output-type dsl-expression))
        rules (:rules dsl-expression)
        output-sym (if (.endsWith output-type *incremental-indicator-suffix*)
                     'output)
        let-body-creation-fn (condp (fn [^String v ^String s] (.startsWith s v)) output-type
                               "java-map" create-let-body-vec-java-map-out
                               "clj-map" create-let-body-vec-clj-map-out
                               "csv-str" create-let-body-vec-csv-str-out
                               "json-str" create-let-body-vec-json-str-out
                               (do
                                 (println "Unknown output type:" output-type)
                                 (println "Defaulting to :java-map as output type.")
                                 create-let-body-vec-java-map-out))
;        _ (println "Created data processing function vector from DSL:" fn-body-vec)
        fn-body (create-let-expression input-sym rules let-body-creation-fn output-sym 0)
;        _ (println "Created data processing function body:" fn-body)
        _ (pprint fn-body)
        _ (println "")
        data-processing-fn (if (not (nil? output-sym))
                             (eval `(fn [~input-sym ~output-sym] ~fn-body))
                             (eval `(fn [~input-sym] ~fn-body)))]
    data-processing-fn))

(defn combine-proc-fns
  "Based on the DSL expression dsl-expression, create a data processing function in which the processing rules starting at start-idx, inclusive, up to end-idx, non inclusive, are combined.
   Please note that it is usually more appropriate to use combine-proc-fns-vec."
  [dsl-expression start-idx end-idx]
  (if (= 0 start-idx)
    (create-proc-fn
      {:output-type (:output-type dsl-expression)
       :rules (subvec (:rules dsl-expression) start-idx end-idx)})
    (create-proc-fn
      {:output-type (keyword (str (name (:output-type dsl-expression)) *incremental-indicator-suffix*))
       :rules (subvec (:rules dsl-expression) start-idx end-idx)})))

(defn combine-proc-fns-vec
  "Based on the function mapping fn-mapping and the DSL expression dsl-expression, create a vector of data processing functions.
   The mapping definition defines the number of processing rules to be included in each processing function.
   For a vector of processing rules [a b c d e f], a mapping definition [1 2 3] will result in the following association of processing rules to processing functions f_x: [f_1(a), f_2(b, c), f_3(d e f)]."
  [fn-mapping dsl-expression]
  (reduce
    (fn [v m]
      (let [start-idx (reduce + (subvec fn-mapping 0 (count v)))]
        (conj v (combine-proc-fns dsl-expression
                                  start-idx
                                  (+ start-idx m)))))
    (let [f (combine-proc-fns dsl-expression 0 (first fn-mapping))]
      [(fn [in _] (f in))])
    (rest fn-mapping)))

