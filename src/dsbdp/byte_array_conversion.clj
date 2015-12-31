;;;
;;;   Copyright 2015 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Helper functions for converting byte arrays into other data types/representations"}
  dsbdp.byte-array-conversion
  (:import
    (dsbdp ByteArrayHelper)
    (java.text SimpleDateFormat)
    (java.util Date)))

(defn int4l
  "Get the lower 4 bits (nibble) of the byte at the given index idx in the provided byte-array ba."
  [ba idx]
  (ByteArrayHelper/getInt4L ba idx))

(defn int4h
  "Get the higher 4 bits (nibble) of the byte at the given index idx in the provided byte-array ba."
  [ba idx]
  (ByteArrayHelper/getInt4H ba idx))

(defn int8
  "Get the byte at the index idx in the byte-array ba."
  [ba idx]
  (ByteArrayHelper/getInt8 ba idx))

(defn int16
  "Get the Int16 value of the two bytes starting at index idx in the byte-array ba."
  [ba idx]
  (ByteArrayHelper/getInt16 ba idx))

(defn int16be
  "Get the big endian Int16 value of the two bytes starting at index idx in the byte-array ba."
  [ba idx]
  (ByteArrayHelper/getInt16BigEndian ba idx))

(defn int32
  "Get the Int32 value of the four bytes starting at index idx in the byte-array ba."
  [ba idx]
  (ByteArrayHelper/getInt32 ba idx))

(defn int32be
  "Get the big endian Int32 value of the four bytes starting at index idx in the byte-array ba."
  [ba idx]
  (ByteArrayHelper/getInt32BigEndian ba idx))

(defn timestamp
  "Get the (pcap) timestamp value of the four bytes starting at index idx in the byte-array ba."
  [ba idx]
  (+ (* (ByteArrayHelper/getInt32 ba idx) 1000000000) (* (ByteArrayHelper/getInt32 ba (+ idx 4)) 1000)))

(defn timestamp-be
  "Get the (pcap) big endian timestamp value of the four bytes starting at index idx in the byte-array ba."
  [ba idx]
  (+ (* (ByteArrayHelper/getInt32BigEndian ba idx) 1000000000) (* (ByteArrayHelper/getInt32BigEndian ba (+ idx 4)) 1000)))

(defn eth-mac-addr-str
  [ba idx]
  (ByteArrayHelper/getEthMacAddrString ba idx))

(defn ipv4-addr-str
  [ba idx]
  (ByteArrayHelper/getIpv4AddrString ba idx))

(defn timestamp-to-str
  [ts]
  (->
    (SimpleDateFormat. "yyyy-MM-DD_HH:mm:ss")
    (.format
      (Date.
        (long (/ ts 1000000))))))

(defn timestamp-str
  [ba idx]
  (timestamp-to-str (timestamp ba idx)))

(defn timestamp-str-be
  [ba idx]
  (timestamp-to-str (timestamp-be ba idx)))

