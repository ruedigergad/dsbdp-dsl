;;;
;;;   Copyright 2019 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns 
  ^{:author "Ruediger Gad",
    :doc "Tests for the test CLI"}  
  dsbdp.test.dsl-main
  (:require
    [cli4clj.cli-tests :as cli-tests]
    [clj-assorted-utils.util :as utils]
    [clojure.test :as test]
    [dsbdp.dsl-main :as dsl-main]))


(test/deftest load-dsl-test
  (let [test-cmd-input ["load-dsl test/data/pcap_file_example_dsl.txt"]
        out-string (cli-tests/test-cli-stdout #(dsl-main/-main "") test-cmd-input)]
    (test/is (=
               (cli-tests/expected-string
                 ["Loading DSL from: test/data/pcap_file_example_dsl.txt"
                  "Setting DSL expression..."
                  "Setting processing function..."
                  "Processing function set."])
               out-string))))

