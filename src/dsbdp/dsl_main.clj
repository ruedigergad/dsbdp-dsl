;;;
;;;   Copyright 2019 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Main class for launching a test CLI."}
  dsbdp.dsl-main
  (:require
    (cli4clj [cli :as cli])
    (clj-assorted-utils [util :as utils])
    (dsbdp
      [data-processing-dsl :as dsl]))
  (:gen-class))

(defn -main [& args]
  (let [dsl-string (atom nil)
        dsl-expression (atom nil)
        _ (add-watch dsl-string nil (fn [_ _ _ n]
                                      (if (not (empty? n))
                                        (try
                                          (println "Setting DSL expression...")
                                          (reset! dsl-expression (read-string n))
                                          (catch Exception e
                                            (println "Error reading DSL string:" (str n))
                                            (.printStackTrace e)))
                                        (println "Error: DSL string is empty."))
                                      nil))
        proc-fn (atom (fn [_] (println "This is a no-op processing function stub."
                                       "Please specify a valid DSL string to set a meaningful processing function.")))
        _ (add-watch dsl-expression nil (fn [_ _ _ n]
                                          (if n
                                            (do
                                              (println "Setting processing function...")
                                              (reset! proc-fn (dsl/create-proc-fn n))
                                              (println "Processing function set."))
                                            (println "Error: cannot set processing function from invalid DSL expression:" (str n)))))]
    (cli/start-cli
      {:cmds {:load-dsl {:fn (fn [dsl-file-name]
                               (println "Loading DSL from:" dsl-file-name)
                               (let [s (slurp (str dsl-file-name))]
                                 (if (not (empty? s))
                                   (reset! dsl-string s)
                                   (println "Failed to read DSL from:" dsl-file-name))
                                 nil))}}})))

