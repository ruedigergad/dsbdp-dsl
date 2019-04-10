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
  dsbdp.dsl.test.main-tests
  (:require
    [cli4clj.cli-tests :as cli-tests]
    [clj-assorted-utils.util :as utils]
    [clojure.test :as test]
    [dsbdp.dsl.main :as dsl-main]))

(def pcap-data-hex "d4c3b2a10200040000000000000000000000040001000000f9aed957afe308006200000062000000e8de27590eadb4749ffa7b920800450000541d8d00004001b8dd0a0000e6951404450800115f112b000057d9aef90008e39708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363706afd9578dbb02004600000046000000e8de27590eadb4749ffa7b920800450000381d980000401148370a0000e60a000001a9cf0035002495c18d3d01000001000000000000066b65726e656c036f7267000001000106afd957a5c602004a0000004a000000e8de27590eadb4749ffa7b9208004500003c1d9a400040061dae0a0000e6c7cc2cc2c17600503da3adf100000000a002ffffa43f0000020405b4010303060402080a1315e3dc00000000")

(def pcap-expected-output
  ["{\"magic-number\" 2712847316,"
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

(def expected-dsl
  ["{:output-type :clj,"
   " :rules"" [[magic-number (int32be 0)]"
   "  [snapshot-len (int32be 16)]"
   "  [packets""   ([capture-length (int32be (+ offset 8))]"
   "    [packet-length (int32be (+ offset 12))]"
   "    [__offset-increment (+ 16 __1_capture-length)]"
   "    [data""     [[eth-dst (eth-mac-addr-str (+ offset 16))]"
   "      [eth-src (eth-mac-addr-str (+ offset 20))]"
   "      [data""       [[proto-id (int8 (+ offset 39))]"
   "        [ip-dst (ipv4-addr-str (+ offset 42))]"
   "        [ip-src (ipv4-addr-str (+ offset 46))]"
   "        [data"
   "         [(= __3_proto-id 1)"
   "          [[type"
   "            (condp"
   "             ="
   "             (int8 (+ offset 50))"
   "             0"
   "             \"Echo Reply\""
   "             3"
   "             \"Destination Unreachable\""
   "             8"
   "             \"Echo Request\")]]"
   "          (= __3_proto-id 6)"
   "          [[dst (int16 (+ offset 52))]"
   "           [src (int16 (+ offset 50))]"
   "           [flags (int8 (+ offset 8))]"
   "           [ack-no (int32 (+ offset 58))]"
   "           [seq-no (int32 (+ offset 54))]]"
   "          (= __3_proto-id 17)"
   "          [[dst (int16 (+ offset 52))]"
   "           [src (int16 (+ offset 50))]]]]]]]])"
   "   {:initial-offset 24}]]}"])

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
               (cli-tests/expected-string
                 (into ["Loading DSL from: test/data/pcap_file_example_dsl.txt"
                        "Setting DSL expression..."
                        "Setting processing function..."
                        "Processing function set."
                        "Processing file: test/data/pcap_three_packets_icmp_dns_http.pcap"]
                       pcap-expected-output))
               out-string))))

(test/deftest process-hex-string
  (let [test-cmd-input ["load-dsl test/data/pcap_file_example_dsl.txt"
                        (str "proc-hex-str " pcap-data-hex)]
        out-string (cli-tests/test-cli-stdout #(dsl-main/-main "") test-cmd-input)]
    (test/is (=
               (cli-tests/expected-string
                 (into ["Loading DSL from: test/data/pcap_file_example_dsl.txt"
                        "Setting DSL expression..."
                        "Setting processing function..."
                        "Processing function set."]
                       pcap-expected-output))
               out-string))))

(test/deftest show-dsl-test
  (let [test-cmd-input ["load-dsl test/data/pcap_file_example_dsl.txt"
                        "show-dsl"]
        out-string (cli-tests/test-cli-stdout #(dsl-main/-main "") test-cmd-input)]
    (test/is (=
               (cli-tests/expected-string
                 (into ["Loading DSL from: test/data/pcap_file_example_dsl.txt"
                        "Setting DSL expression..."
                        "Setting processing function..."
                        "Processing function set."]
                       expected-dsl))
               out-string))))

