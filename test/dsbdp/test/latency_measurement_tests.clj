;;;
;;;   Copyright 2016 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Unit tests for measuring latency."}
  dsbdp.test.latency-measurement-tests
  (:require [clojure.test :refer :all])
  (:import (dsbdp LatencyProbe )))

(deftest simple-latency-probe-test
  (let [latency-probe (LatencyProbe.)]
    (.done latency-probe)
    (is (< 0 (.getDelta latency-probe)))))

