;;;
;;;   Copyright 2016 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Functions for self-adaptive local data processing pipelines."}
  dsbdp.local-dpp-self-adaptivity
  (:require
    [clj-assorted-utils.util :refer :all]
    [clojure.pprint :refer :all]))

(defn create-stat-delta-counter
  [n]
  (let [delta-cntr (delta-counter)]
    (doseq [e (reduce
                (fn [m n]
                  (assoc m n {:out 0, :dropped 0}))
                {:pipeline {:in 0, :dropped 0}}
                (range 0 n))]
      (let [k (key e)
            v (val e)]
        (if (= :pipeline k)
          (delta-cntr (keyword (str k "-in")) (:in v))
          (delta-cntr (keyword (str k "-out")) (:out v)))
        (delta-cntr (keyword (str k "-dropped")) (:dropped v))))
    (fn [current-stats]
      (reduce
        (fn [m e]
          (let [k (key e)
                v (val e)]
            (assoc m k
                   (if (= :pipeline k)
                     {:in-delta (delta-cntr (keyword (str k "-in")) (:in v))
                      :dropped-delta (delta-cntr (keyword (str k "-dropped")) (:dropped v))}
                     {:out-delta (delta-cntr (keyword (str k "-out")) (:out v))
                      :dropped-delta (delta-cntr (keyword (str k "-dropped")) (:dropped v))}))))
        {}
        current-stats))))

(defn create-repetition-detector
  [repetitions]
  (let [cntr (counter)]
    (fn [pred-f]
      (if (>= (cntr) repetitions)
        (cntr (fn [_] 0)))
      (if (pred-f)
        (cntr inc)
        (cntr (fn [_] 0)))
      (= repetitions (cntr)))))

(defn create-moving-average-calculator
  [cnt]
  (let [data (ref (vec (repeat cnt 0)))]
    (fn
      ([]
        (/ (apply + @data) cnt))
      ([value]
        (dosync (alter data (fn [d v] (-> d (subvec 1) (conj v))) value))))))

(defn create-drop-detector
  [n-repetitions n-detectors threshold]
  (let [rep-detectors (reduce
                        (fn [v _] (conj v (create-repetition-detector n-repetitions)))
                        []
                        (repeat n-detectors 0))]
    (fn [deltas]
      (reduce
        (fn [v e]
          (conj v ((rep-detectors (inc (key e))) #(> (:dropped-delta (val e)) threshold))))
        [((rep-detectors 0) #(> (get-in deltas [:pipeline :dropped-delta]) threshold))]
        (sort (dissoc deltas :pipeline))))))

(defn get-drop-indices
  [drop-det-vec]
  (vec
    (reduce-kv
      (fn [o k v] (if v (conj o k) o))
      []
      drop-det-vec)))

(defn get-non-drop-indices
  [drop-det-vec]
  (vec
    (reduce-kv
      (fn [o k v] (if (not v) (conj o k) o))
      []
      drop-det-vec)))

(defn create-mapping-updater
  []
  (fn [orig-mapping drop-det-vec]
    (let [fn-drops (subvec drop-det-vec 0 (dec (count drop-det-vec)))]
    (cond
      (every? #(= false %) fn-drops) orig-mapping
      :default nil
      ))
    )
  )

