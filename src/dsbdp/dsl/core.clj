;;;
;;;   Copyright 2015 - 2019 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "DSL for processing data"}
  dsbdp.dsl.core
  (:require [dsbdp.dsl.byte-array-conversion :refer :all]
            [clojure.pprint :refer :all]))

(def ^:dynamic *incremental-indicator-suffix* "#inc")
(def ^:dynamic *verbose* false)

(def offset-suffix "__offset")

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
                  (or (= s 'nth) (= s 'get) (= s 'identity))
                    (conj v (ns-resolve 'clojure.core s) 'input)
                  (ns-resolve 'clojure.core s)
                    (conj v (ns-resolve 'clojure.core s))
                  (ns-resolve 'dsbdp.dsl.byte-array-conversion s)
                    (conj v (ns-resolve 'dsbdp.dsl.byte-array-conversion s) 'input)
                  :default
                    (do
                      (when *verbose*
                        (println "Warning: Could not resolve symbol:" s)
                        (println "Assuming" s "is intended as \"self-reference\"."))
                      (conj v s))))
            (list? data-proc-def-element)
              (conj v (into '() (reverse (create-proc-sub-fn data-proc-def-element input))))
            :default (conj v data-proc-def-element)))
        [] data-processing-definition))))

(defn- cond-rule-expr?
  "True if the supplied rule-rexpression is a \"cond rule expression\" that contains processing alternatives."
  [rule-expression]
  (and
    (vector? rule-expression)
    (every? list? (butlast (take-nth 2 rule-expression)))
    (every?
      vector?
        (butlast
          (take-nth
            2
            (rest rule-expression))))))

(defn prefix-rule-name
  "Prefix the rule name based on its nesting level to assure that at identifiers at each branch level are unique."
  [rule-name nesting-level]
  (if
    (> 1 nesting-level)
    (symbol rule-name)
    (symbol (str "__" nesting-level "_" rule-name))))

(declare create-let-expression)
(declare create-proc-fn)

(defn- with-offsets?
  "True if :with-offsets is set and the rule expression contains an offset expression."
  [rule dsl-expression]
  (let [rule-name (name (first rule))
        rule-expression (second rule)
        offset (second rule-expression)]
    (and
      (:with-offsets dsl-expression)
      (not (.startsWith rule-name "__"))
      (or (number? offset) (list? offset)))))

(defn- create-bindings-vector
  "Create the bindings vector for the let expression created with create-let-expression."
  [input rules out-format-fn output nesting-level dsl-expression]
  (reduce
    (fn [v rule]
      (let [rule-name (first rule)
            rule-expression (second rule)]
        (cond
          (and (list? rule-expression) (every? vector? rule-expression)) 
          (let [seq-params (rule 2)
                nested-expr-tmp (create-let-expression
                                  input
                                  (vec rule-expression)
                                  out-format-fn
                                  output
                                  (inc nesting-level)
                                  dsl-expression)
                nested-expr-ret-tmp (last nested-expr-tmp)
                nested-expr-ret [nested-expr-ret-tmp (prefix-rule-name '__offset-increment (inc nesting-level))]
                nested-expr (into '() (reverse (assoc (vec nested-expr-tmp) (- (count nested-expr-tmp) 1) nested-expr-ret)))
                loop-expr `(loop [~'offset (~seq-params :initial-offset) ~'result ~(out-format-fn)]
                            (let [~'tmp-result ~nested-expr
                                  ~'new-offset (+ ~'offset (second ~'tmp-result))
                                  ~'new-result ~(out-format-fn 'result '(first tmp-result))]
                              (if (< ~'new-offset (count ~'input))
                                (recur ~'new-offset ~'new-result)
                                ~'new-result)))]
            (conj v
                  (prefix-rule-name rule-name nesting-level)
                  loop-expr))

          (list? rule-expression)
          (cond-> v
            (with-offsets? rule dsl-expression) (conj (prefix-rule-name (str (name rule-name) offset-suffix) nesting-level) (second rule-expression))
            true (conj
                   (prefix-rule-name rule-name nesting-level)
                   (create-proc-sub-fn
                     (if (with-offsets? rule dsl-expression)
                       (replace {2 (prefix-rule-name (str (name rule-name) offset-suffix) nesting-level)} rule-expression)
                       rule-expression)
                     input)))

          (and (vector? rule-expression) (every? vector? rule-expression))
          (let [nested-expr (create-let-expression
                              input
                              rule-expression
                              out-format-fn
                              output
                              (inc nesting-level)
                              dsl-expression)]
            (conj v (prefix-rule-name rule-name nesting-level) nested-expr))

          (cond-rule-expr? rule-expression)
          (let [cond-expr (reduce
                            (fn [vect v]
                              (cond
                                (or (list? v)
                                    (keyword? v)) (conj vect v)
                                (vector? v) (conj vect
                                                  (create-let-expression
                                                    input
                                                    v
                                                    out-format-fn
                                                    output
                                                    (inc nesting-level)
                                                    dsl-expression))
                                :default (do
                                           (println "Unknown rule expression part:" v)
                                           vect)))
                            ['clojure.core/cond]
                            rule-expression)]
            (conj v (prefix-rule-name rule-name nesting-level) (into '() (reverse cond-expr))))

          :default (println "Binding: unknown element for rule:" (str rule)))))
    []
    rules))

(defn- create-let-expression
  "Create let expression based on the given DSL rules etc."
  [input rules out-format-fn output nesting-level dsl-expression]
  `(let
    ~(create-bindings-vector input rules out-format-fn output nesting-level dsl-expression)
    ~(reverse (into '() (out-format-fn rules output nesting-level dsl-expression)))))

(defn- java-out-format-fn
  "Output function for producing Java-style output."
  ([]
    `(java.util.ArrayList.))
  ([result-list new-result-value]
    `(doto ~result-list (.add ~new-result-value)))
  ([rules output nesting-level dsl-expression]
    (reduce
      (fn [v rule]
        (cond
          (sequential? (second rule))
          (cond-> v
            true (conj `(.put
                         ~(name (first rule))
                         ~(prefix-rule-name (first rule) nesting-level)))
            (with-offsets? rule dsl-expression) (conj `(.put
                                                        ~(str (name (first rule)) offset-suffix)
                                                        ~(prefix-rule-name (str (name (first rule)) offset-suffix) nesting-level))))

          (cond-rule-expr? (second rule))
          (conj v
            `(.put ~(name (first rule))
                   ~(prefix-rule-name (first rule) nesting-level)))

          :default (println "Java Map Body: unknown element for rule:" (str rule))))
      (if (nil? output)
        '[doto (java.util.HashMap.)]
        '[doto ^java.util.Map output])
      rules)))

(defn- clj-out-format-fn
  "Output function for producing Clojure-style output."
  ([]
    [])
  ([result-vec new-result-value]
    `(conj ~result-vec ~new-result-value))
  ([rules output nesting-level dsl-expression]
    (reduce
      (fn [v rule]
        (cond
          (sequential? (second rule))
          (cond-> v
            true (conj `(assoc
                          ~(name (first rule))
                          ~(prefix-rule-name (first rule) nesting-level)))
            (with-offsets? rule dsl-expression) (conj `(assoc
                                                        ~(str (name (first rule)) offset-suffix)
                                                        ~(prefix-rule-name (str (name (first rule)) offset-suffix) nesting-level))))

          (cond-rule-expr? (second rule))
          (conj v `(assoc
                     ~(name (first rule))
                     ~(prefix-rule-name (first rule) nesting-level)))

          :default
          (do (println "Clj Map Body: unknown element for rule:" (str rule)))))
      (if (nil? output)
        '[-> {}]
        '[-> output])
      rules)))

(defn- csv-str-out-format-fn
  "Output function for producing CSV-style output."
  [rules output nesting-level dsl-expression]
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

(defn- json-str-out-format-fn
  "Output function for producing JSON-style output."
  [rules output nesting-level dsl-expression]
  (reduce
    (fn [v rule]
      (let [tmp-k (conj v `(.append "\"")
                          `(.append ~(name (first rule)))
                          `(.append "\":"))
            tmp-k2 (if (some #{:string} rule)
                     (conj tmp-k `(.append "\""))
                     tmp-k)
            tmp-v (cond
                    (sequential? (second rule)) (conj
                                                  tmp-k2
                                                  `(.append ~(prefix-rule-name (first rule) nesting-level)))
                    (cond-rule-expr? (second rule)) (conj tmp-k2 `(.append ~(prefix-rule-name (first rule) nesting-level)))
                    :default (do
                               (println "JSON String Body: unknown element for rule:" (str rule))
                               tmp-k2))
            tmp-v2 (if (some #{:string} rule)
                     (conj tmp-v `(.append "\""))
                     tmp-v)]
        (if (not= rule (last rules))
          (conj tmp-v2 `(.append ","))
          (conj tmp-v2 `(.append "}")))))
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
        output-format-fn (condp (fn [^String v ^String s] (.startsWith s v)) output-type
                           "java" java-out-format-fn
                           "java-map" (do
                                        (println "The format string 'java-map' is deprecated. Please use 'java' instead.")
                                        java-out-format-fn)
                           "clj" clj-out-format-fn
                           "clj-map" (do
                                       (println "The format string 'clj-map' is deprecated. Please use 'clj' instead.")
                                       clj-out-format-fn)
                           "csv-str" csv-str-out-format-fn
                           "json-str" json-str-out-format-fn
                           (do
                             (println "Unknown output type:" output-type)
                             (println "Defaulting to :java-map as output type.")
                             java-out-format-fn))
;        _ (println "Created data processing function vector from DSL:" fn-body-vec)
        fn-body (create-let-expression input-sym rules output-format-fn output-sym 0 dsl-expression)
;        _ (println "Created data processing function body:" fn-body)
;        _ (pprint fn-body)
;        _ (println "")
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

