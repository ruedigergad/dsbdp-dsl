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

(def four-staged-pipeline-stats-example
  {3 {:out 17460, :dropped 1082},
   2 {:out 18542, :dropped 1697469},
   1 {:out 1716523, :dropped 611647},
   0 {:out 2328170, :dropped 2911553},
   :pipeline {:in 5239723, :dropped 7764598}})

