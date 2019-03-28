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
    (clojure
      [pprint :as pprint]
      [string :as string])
    (dsbdp
      [data-processing-dsl :as dsl]))
  (:import
    (java.nio.file Files Paths)
    (javax.xml.bind DatatypeConverter))
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
        proc-fn (atom (fn [_] (println "This is a no-op processing function place-holder."
                                       "Please use load-dsl to specify a valid DSL instance to set a meaningful processing function.")))
        _ (add-watch dsl-expression nil (fn [_ _ _ n]
                                          (if n
                                            (try
                                              (println "Setting processing function...")
                                              (reset! proc-fn (dsl/create-proc-fn n))
                                              (println "Processing function set.")
                                              (catch Exception e
                                                (println "Error setting the processing function from:" (str n))
                                                (.printStackTrace e)))
                                            (println "Error: cannot set processing function from invalid DSL expression:" (str n)))))]
    (cli/start-cli
      {:cmds
       {:load-dsl {:fn (fn [dsl-file-name]
                         (println "Loading DSL from:" dsl-file-name)
                         (let [s (slurp (str dsl-file-name))]
                           (if (not (empty? s))
                             (reset! dsl-string s)
                             (println "Failed to read DSL from:" dsl-file-name))
                           nil))}
        :proc-bin-file {:fn (fn [bin-file-name]
                              (if (utils/file-exists? (str bin-file-name))
                                (try
                                  (println "Processing file:" (str bin-file-name))
                                  (let [in-data (Files/readAllBytes (Paths/get (str bin-file-name) (into-array [""])))
                                        out-data (@proc-fn in-data)]
                                    (pprint/pprint out-data))
                                  (catch Exception e
                                    (println "Error processing binary file:" (str bin-file-name))
                                    (.printStackTrace e)))
                                (println "Error file does not exits:" (str bin-file-name))))}
        :proc-hex-str {:fn (fn [hex-string]
                             (if (not (empty? (str hex-string)))
                               (try
                                 (-> hex-string str string/trim (DatatypeConverter/parseHexBinary) (@proc-fn) pprint/pprint)
                                 (catch Exception e
                                   (println "Error processing hex string:" (str hex-string))
                                   (.printStackTrace e)))
                               (println "Error: Cannot process empty string."))
                             nil)
                       :long-info "A hex string suitable for processing can be obtained from a file, e.g., as follows: xxd -p <FILE> | tr -d '\\n'"}}})))

