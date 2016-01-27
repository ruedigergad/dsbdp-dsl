;;;
;;;   Copyright 2016 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Unit tests for local self-adaptive data processing pipelines."}
  dsbdp.test.local-dpp-self-adaptivity-tests
  (:require
    [clojure.test :refer :all]
    [dsbdp.local-dpp-self-adaptivity :refer :all]))

(def four-staged-pipeline-stats-example-1
  {3 {:out 17460, :dropped 1082},
   2 {:out 18542, :dropped 1697469},
   1 {:out 1716523, :dropped 611647},
   0 {:out 2328170, :dropped 2911553},
   :pipeline {:in 5239723, :dropped 7764598}})

(def four-staged-pipeline-stats-example-2
  {3 {:out 18460, :dropped 2082},
   2 {:out 19542, :dropped 1698469},
   1 {:out 1717523, :dropped 612647},
   0 {:out 2329170, :dropped 2912553},
   :pipeline {:in 5240723, :dropped 7765598}})

(deftest stat-delta-test-1
  (let [stat-1 {0 {:out 1, :dropped 2}, 1 {:out 3, :dropped 4}, :pipeline {:in 5, :dropped 6}}
        stat-2 {0 {:out 10, :dropped 20}, 1 {:out 30, :dropped 40}, :pipeline {:in 50, :dropped 60}}
        expected-delta-1 {0 {:out-delta 1, :dropped-delta 2}, 1 {:out-delta 3, :dropped-delta 4},
                          :pipeline {:in-delta 5, :dropped-delta 6}}
        expected-delta-2 {0 {:out-delta 9, :dropped-delta 18}, 1 {:out-delta 27, :dropped-delta 36},
                          :pipeline {:in-delta 45, :dropped-delta 54}}
        stat-d-cntr (create-stat-delta-counter 2)]
    (is (= expected-delta-1 (stat-d-cntr stat-1)))
    (is (= expected-delta-2 (stat-d-cntr stat-2)))))

(deftest stat-delta-test-2
  (let [expected-delta-1 {0 {:out-delta 2328170, :dropped-delta 2911553}, 1 {:out-delta 1716523, :dropped-delta 611647},
                          2 {:out-delta 18542, :dropped-delta 1697469}, 3 {:out-delta 17460, :dropped-delta 1082},
                          :pipeline {:in-delta 5239723, :dropped-delta 7764598}}
        expected-delta-2 {0 {:out-delta 1000, :dropped-delta 1000}, 1 {:out-delta 1000, :dropped-delta 1000},
                          2 {:out-delta 1000, :dropped-delta 1000}, 3 {:out-delta 1000, :dropped-delta 1000},
                          :pipeline {:in-delta 1000, :dropped-delta 1000}}
        stat-d-cntr (create-stat-delta-counter 4)]
    (is (= expected-delta-1 (stat-d-cntr four-staged-pipeline-stats-example-1)))
    (is (= expected-delta-2 (stat-d-cntr four-staged-pipeline-stats-example-2)))))

(deftest simple-repetition-detection-test-1
  (let [detector (create-repetition-detector 3)]
    (is (not (detector (fn [] true))))
    (is (not (detector (fn [] true))))
    (is (detector (fn [] true)))
    (is (not (detector (fn [] true))))))

(deftest simple-repetition-detection-test-2
  (let [detector (create-repetition-detector 3)]
    (is (not (detector (fn [] true))))
    (is (not (detector (fn [] true))))
    (is (not (detector (fn [] false))))
    (is (not (detector (fn [] true))))
    (is (not (detector (fn [] true))))
    (is (detector (fn [] true)))
    (is (not (detector (fn [] true))))))

(deftest moving-average-calculator-test-1
  (let [mvg-avg-calc (create-moving-average-calculator 3)]
    (is (= 0 (mvg-avg-calc)))
    (mvg-avg-calc 1000)
    (is (= (/ 1000 3) (mvg-avg-calc)))
    (mvg-avg-calc 1000)
    (is (= (/ 2000 3) (mvg-avg-calc)))
    (mvg-avg-calc 1000)
    (is (= 1000 (mvg-avg-calc)))))

(deftest moving-average-calculator-test-2
  (let [mvg-avg-calc (create-moving-average-calculator 3)]
    (mvg-avg-calc 1000)
    (mvg-avg-calc 2000)
    (mvg-avg-calc 3000)
    (is (= 2000 (mvg-avg-calc)))))

(deftest moving-average-calculator-test-3
  (let [mvg-avg-calc (create-moving-average-calculator 3)]
    (mvg-avg-calc 1000)
    (mvg-avg-calc 1000)
    (mvg-avg-calc 1000)
    (mvg-avg-calc 2000)
    (mvg-avg-calc 3000)
    (is (= 2000 (mvg-avg-calc)))))

(deftest detect-repeated-dropped-test-1
  (let [deltas {0 {:out-delta 10, :dropped-delta 10}, 1 {:out-delta 10, :dropped-delta 10},
                :pipeline {:in-delta 10, :dropped-delta 10}}
        detector (create-drop-detector 3 (count deltas) 5)]
    (is (= [false false false] (detector deltas)))
    (is (= [false false false] (detector deltas)))
    (is (= [true true true] (detector deltas)))))

(deftest detect-repeated-dropped-test-2
  (let [deltas {0 {:out-delta 10, :dropped-delta 10}, 1 {:out-delta 10, :dropped-delta 10},
                2 {:out-delta 10, :dropped-delta 10}, 3 {:out-delta 10, :dropped-delta 10},
                :pipeline {:in-delta 10, :dropped-delta 10}}
        detector (create-drop-detector 4 (count deltas) 5)]
    (is (= [false false false false false] (detector deltas)))
    (is (= [false false false false false] (detector deltas)))
    (is (= [false false false false false] (detector deltas)))
    (is (= [true true true true true] (detector deltas)))))

