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
  (:import (dsbdp ByteArrayHelper)))

(defn int32
  "Get the Int32 value of the four bytes starting at index idx in the byte-array ba."
  [ba idx]
  (ByteArrayHelper/getInt ba idx))

(defn int32be
  "Get the big endian Int32 value of the four bytes starting at index idx in the byte-array ba."
  [ba idx]
  (ByteArrayHelper/getIntBigEndian ba idx))

(defn timestamp
  "Get the (pcap) timestamp value of the four bytes starting at index idx in the byte-array ba."
  [ba idx]
  (+ (* (ByteArrayHelper/getInt ba idx) 1000000000) (* (ByteArrayHelper/getInt ba (+ idx 4)) 1000)))

(defn timestamp-be
  "Get the (pcap) big endian timestamp value of the four bytes starting at index idx in the byte-array ba."
  [ba idx]
  (+ (* (ByteArrayHelper/getIntBigEndian ba idx) 1000000000) (* (ByteArrayHelper/getIntBigEndian ba (+ idx 4)) 1000)))

