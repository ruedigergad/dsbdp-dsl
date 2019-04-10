$(function() {
  var index = [
    {label: "dsbdp.dsl.DslHelper/-generateProcessingFn", value: "dsbdp.dsl.DslHelper.html#ID-generateProcessingFn"},
    {label: "dsbdp.dsl.DslHelper/-prettyPrint", value: "dsbdp.dsl.DslHelper.html#ID-prettyPrint"},
    {label: "dsbdp.dsl.byte-array-conversion/ba-to-str", value: "dsbdp.dsl.byte-array-conversion.html#IDba-to-str"},
    {label: "dsbdp.dsl.byte-array-conversion/eth-mac-addr-str", value: "dsbdp.dsl.byte-array-conversion.html#IDeth-mac-addr-str"},
    {label: "dsbdp.dsl.byte-array-conversion/int-to-byte", value: "dsbdp.dsl.byte-array-conversion.html#IDint-to-byte"},
    {label: "dsbdp.dsl.byte-array-conversion/int16", value: "dsbdp.dsl.byte-array-conversion.html#IDint16"},
    {label: "dsbdp.dsl.byte-array-conversion/int16be", value: "dsbdp.dsl.byte-array-conversion.html#IDint16be"},
    {label: "dsbdp.dsl.byte-array-conversion/int32", value: "dsbdp.dsl.byte-array-conversion.html#IDint32"},
    {label: "dsbdp.dsl.byte-array-conversion/int32be", value: "dsbdp.dsl.byte-array-conversion.html#IDint32be"},
    {label: "dsbdp.dsl.byte-array-conversion/int4h", value: "dsbdp.dsl.byte-array-conversion.html#IDint4h"},
    {label: "dsbdp.dsl.byte-array-conversion/int4l", value: "dsbdp.dsl.byte-array-conversion.html#IDint4l"},
    {label: "dsbdp.dsl.byte-array-conversion/int64", value: "dsbdp.dsl.byte-array-conversion.html#IDint64"},
    {label: "dsbdp.dsl.byte-array-conversion/int64be", value: "dsbdp.dsl.byte-array-conversion.html#IDint64be"},
    {label: "dsbdp.dsl.byte-array-conversion/int8", value: "dsbdp.dsl.byte-array-conversion.html#IDint8"},
    {label: "dsbdp.dsl.byte-array-conversion/ipv4-addr-str", value: "dsbdp.dsl.byte-array-conversion.html#IDipv4-addr-str"},
    {label: "dsbdp.dsl.byte-array-conversion/timestamp", value: "dsbdp.dsl.byte-array-conversion.html#IDtimestamp"},
    {label: "dsbdp.dsl.byte-array-conversion/timestamp-be", value: "dsbdp.dsl.byte-array-conversion.html#IDtimestamp-be"},
    {label: "dsbdp.dsl.byte-array-conversion/timestamp-str", value: "dsbdp.dsl.byte-array-conversion.html#IDtimestamp-str"},
    {label: "dsbdp.dsl.byte-array-conversion/timestamp-str-be", value: "dsbdp.dsl.byte-array-conversion.html#IDtimestamp-str-be"},
    {label: "dsbdp.dsl.byte-array-conversion/timestamp-to-str", value: "dsbdp.dsl.byte-array-conversion.html#IDtimestamp-to-str"},
    {label: "dsbdp.dsl.core/*incremental-indicator-suffix*", value: "dsbdp.dsl.core.html#IDMULincremental-indicator-suffixMUL"},
    {label: "dsbdp.dsl.core/*verbose*", value: "dsbdp.dsl.core.html#IDMULverboseMUL"},
    {label: "dsbdp.dsl.core/combine-proc-fns", value: "dsbdp.dsl.core.html#IDcombine-proc-fns"},
    {label: "dsbdp.dsl.core/combine-proc-fns-vec", value: "dsbdp.dsl.core.html#IDcombine-proc-fns-vec"},
    {label: "dsbdp.dsl.core/create-proc-fn", value: "dsbdp.dsl.core.html#IDcreate-proc-fn"},
    {label: "dsbdp.dsl.core/offset-suffix", value: "dsbdp.dsl.core.html#IDoffset-suffix"},
    {label: "dsbdp.dsl.core/prefix-rule-name", value: "dsbdp.dsl.core.html#IDprefix-rule-name"},
    {label: "dsbdp.dsl.experiment-helper/pcap-byte-array-test-data", value: "dsbdp.dsl.experiment-helper.html#IDpcap-byte-array-test-data"},
    {label: "dsbdp.dsl.experiment-helper/pcap-icmp-byte-array-test-data", value: "dsbdp.dsl.experiment-helper.html#IDpcap-icmp-byte-array-test-data"},
    {label: "dsbdp.dsl.experiment-helper/pcap-tcp-byte-array-test-data", value: "dsbdp.dsl.experiment-helper.html#IDpcap-tcp-byte-array-test-data"},
    {label: "dsbdp.dsl.experiment-helper/sample-pcap-processing-definition-clj-map", value: "dsbdp.dsl.experiment-helper.html#IDsample-pcap-processing-definition-clj-map"},
    {label: "dsbdp.dsl.experiment-helper/sample-pcap-processing-definition-csv", value: "dsbdp.dsl.experiment-helper.html#IDsample-pcap-processing-definition-csv"},
    {label: "dsbdp.dsl.experiment-helper/sample-pcap-processing-definition-java-map", value: "dsbdp.dsl.experiment-helper.html#IDsample-pcap-processing-definition-java-map"},
    {label: "dsbdp.dsl.experiment-helper/sample-pcap-processing-definition-json", value: "dsbdp.dsl.experiment-helper.html#IDsample-pcap-processing-definition-json"},
    {label: "dsbdp.dsl.experiment-helper/sample-pcap-processing-definition-rules", value: "dsbdp.dsl.experiment-helper.html#IDsample-pcap-processing-definition-rules"},
    {label: "dsbdp.dsl.main/-main", value: "dsbdp.dsl.main.html#ID-main"}  ];
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

