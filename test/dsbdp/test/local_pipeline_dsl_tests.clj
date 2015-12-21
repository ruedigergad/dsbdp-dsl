;;;
;;;   Copyright 2015 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Unit tests for the local processing pipeline DSL"}
  dsbdp.test.local-pipeline-dsl-tests
  (:require
    [clj-assorted-utils.util :refer :all]
    [clojure.test :refer :all]
    [dsbdp.local-data-processing-pipeline :refer :all]))

