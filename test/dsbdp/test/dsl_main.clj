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

(def pcap-expected-output
  ["Loading DSL from: test/data/pcap_file_example_dsl.txt"
   "Setting DSL expression..."
   "Setting processing function..."
   "Processing function set."
   "Processing file: test/data/pcap_three_packets_icmp_dns_http.pcap"
   "{\"magic-number\" 2712847316,"
   " \"snapshot-len\" 262144,"
   " \"packets\""
   " [{\"capture-length\" 98,"
   "   \"packet-length\" 98,"
   "   \"__offset-increment\" 114,"
   "   \"data\""
   "   {\"eth-dst\" \"E8:DE:27:59:0E:AD\","
   "    \"eth-src\" \"0E:AD:B4:74:9F:FA\","
   "    \"data\""
   "    {\"proto-id\" 1,"
   "     \"ip-dst\" \"10.0.0.230\","
   "     \"ip-src\" \"149.20.4.69\","
   "     \"data\" {\"type\" \"Echo Request\"}}}}"
   "  {\"capture-length\" 70,"
   "   \"packet-length\" 70,"
   "   \"__offset-increment\" 86,"
   "   \"data\""
   "   {\"eth-dst\" \"E8:DE:27:59:0E:AD\","
   "    \"eth-src\" \"0E:AD:B4:74:9F:FA\","
   "    \"data\""
   "    {\"proto-id\" 17,"
   "     \"ip-dst\" \"10.0.0.230\","
   "     \"ip-src\" \"10.0.0.1\","
   "     \"data\" {\"dst\" 53, \"src\" 43471}}}}"
   "  {\"capture-length\" 74,"
   "   \"packet-length\" 74,"
   "   \"__offset-increment\" 90,"
   "   \"data\""
   "   {\"eth-dst\" \"E8:DE:27:59:0E:AD\","
   "    \"eth-src\" \"0E:AD:B4:74:9F:FA\","
   "    \"data\""
   "    {\"proto-id\" 6,"
   "     \"ip-dst\" \"10.0.0.230\","
   "     \"ip-src\" \"199.204.44.194\","
   "     \"data\""
   "     {\"dst\" 80,"
   "      \"src\" 49526,"
   "      \"flags\" 74,"
   "      \"ack-no\" 0,"
   "      \"seq-no\" 1034137073}}}}]}"])

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

(test/deftest process-binary-file
  (let [test-cmd-input ["load-dsl test/data/pcap_file_example_dsl.txt"
                        "proc-bin-file test/data/pcap_three_packets_icmp_dns_http.pcap"]
        out-string (cli-tests/test-cli-stdout #(dsl-main/-main "") test-cmd-input)]
    (test/is (=
               (cli-tests/expected-string pcap-expected-output)
               out-string))))

