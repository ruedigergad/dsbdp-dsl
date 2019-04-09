$(function() {
  var index = [
    {label: "dsbdp.DslHelper/-generateProcessingFn", value: "dsbdp.DslHelper.html#ID-generateProcessingFn"},
    {label: "dsbdp.DslHelper/-prettyPrint", value: "dsbdp.DslHelper.html#ID-prettyPrint"},
    {label: "dsbdp.byte-array-conversion/ba-to-str", value: "dsbdp.byte-array-conversion.html#IDba-to-str"},
    {label: "dsbdp.byte-array-conversion/eth-mac-addr-str", value: "dsbdp.byte-array-conversion.html#IDeth-mac-addr-str"},
    {label: "dsbdp.byte-array-conversion/int-to-byte", value: "dsbdp.byte-array-conversion.html#IDint-to-byte"},
    {label: "dsbdp.byte-array-conversion/int16", value: "dsbdp.byte-array-conversion.html#IDint16"},
    {label: "dsbdp.byte-array-conversion/int16be", value: "dsbdp.byte-array-conversion.html#IDint16be"},
    {label: "dsbdp.byte-array-conversion/int32", value: "dsbdp.byte-array-conversion.html#IDint32"},
    {label: "dsbdp.byte-array-conversion/int32be", value: "dsbdp.byte-array-conversion.html#IDint32be"},
    {label: "dsbdp.byte-array-conversion/int4h", value: "dsbdp.byte-array-conversion.html#IDint4h"},
    {label: "dsbdp.byte-array-conversion/int4l", value: "dsbdp.byte-array-conversion.html#IDint4l"},
    {label: "dsbdp.byte-array-conversion/int64", value: "dsbdp.byte-array-conversion.html#IDint64"},
    {label: "dsbdp.byte-array-conversion/int64be", value: "dsbdp.byte-array-conversion.html#IDint64be"},
    {label: "dsbdp.byte-array-conversion/int8", value: "dsbdp.byte-array-conversion.html#IDint8"},
    {label: "dsbdp.byte-array-conversion/ipv4-addr-str", value: "dsbdp.byte-array-conversion.html#IDipv4-addr-str"},
    {label: "dsbdp.byte-array-conversion/timestamp", value: "dsbdp.byte-array-conversion.html#IDtimestamp"},
    {label: "dsbdp.byte-array-conversion/timestamp-be", value: "dsbdp.byte-array-conversion.html#IDtimestamp-be"},
    {label: "dsbdp.byte-array-conversion/timestamp-str", value: "dsbdp.byte-array-conversion.html#IDtimestamp-str"},
    {label: "dsbdp.byte-array-conversion/timestamp-str-be", value: "dsbdp.byte-array-conversion.html#IDtimestamp-str-be"},
    {label: "dsbdp.byte-array-conversion/timestamp-to-str", value: "dsbdp.byte-array-conversion.html#IDtimestamp-to-str"},
    {label: "dsbdp.data-processing-dsl/*incremental-indicator-suffix*", value: "dsbdp.data-processing-dsl.html#IDMULincremental-indicator-suffixMUL"},
    {label: "dsbdp.data-processing-dsl/*verbose*", value: "dsbdp.data-processing-dsl.html#IDMULverboseMUL"},
    {label: "dsbdp.data-processing-dsl/combine-proc-fns", value: "dsbdp.data-processing-dsl.html#IDcombine-proc-fns"},
    {label: "dsbdp.data-processing-dsl/combine-proc-fns-vec", value: "dsbdp.data-processing-dsl.html#IDcombine-proc-fns-vec"},
    {label: "dsbdp.data-processing-dsl/create-proc-fn", value: "dsbdp.data-processing-dsl.html#IDcreate-proc-fn"},
    {label: "dsbdp.data-processing-dsl/offset-suffix", value: "dsbdp.data-processing-dsl.html#IDoffset-suffix"},
    {label: "dsbdp.data-processing-dsl/prefix-rule-name", value: "dsbdp.data-processing-dsl.html#IDprefix-rule-name"},
    {label: "dsbdp.dsl-main/-main", value: "dsbdp.dsl-main.html#ID-main"},
    {label: "dsbdp.experiment-helper/pcap-byte-array-test-data", value: "dsbdp.experiment-helper.html#IDpcap-byte-array-test-data"},
    {label: "dsbdp.experiment-helper/pcap-icmp-byte-array-test-data", value: "dsbdp.experiment-helper.html#IDpcap-icmp-byte-array-test-data"},
    {label: "dsbdp.experiment-helper/pcap-tcp-byte-array-test-data", value: "dsbdp.experiment-helper.html#IDpcap-tcp-byte-array-test-data"},
    {label: "dsbdp.experiment-helper/sample-pcap-processing-definition-clj-map", value: "dsbdp.experiment-helper.html#IDsample-pcap-processing-definition-clj-map"},
    {label: "dsbdp.experiment-helper/sample-pcap-processing-definition-csv", value: "dsbdp.experiment-helper.html#IDsample-pcap-processing-definition-csv"},
    {label: "dsbdp.experiment-helper/sample-pcap-processing-definition-java-map", value: "dsbdp.experiment-helper.html#IDsample-pcap-processing-definition-java-map"},
    {label: "dsbdp.experiment-helper/sample-pcap-processing-definition-json", value: "dsbdp.experiment-helper.html#IDsample-pcap-processing-definition-json"},
    {label: "dsbdp.experiment-helper/sample-pcap-processing-definition-rules", value: "dsbdp.experiment-helper.html#IDsample-pcap-processing-definition-rules"}  ];
  $('#api-search').autocomplete({
     source: index,
     focus: function(event, ui) {
       event.preventDefault();
     },
     select: function(event, ui) {
       window.open(ui.item.value, '_self');
       ui.item.value = '';
     }
  });
});

