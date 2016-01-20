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
    [clojure.walk :refer :all]
    [clojure.pprint :refer :all]))

