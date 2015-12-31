/*
 *
 * Copyright (C) 2015 Ruediger Gad
 *
 * This software is released under the terms of the Eclipse Public License 
 * (EPL) 1.0. You can find a copy of the EPL at: 
 * http://opensource.org/licenses/eclipse-1.0.php
 *
 */

package dsbdp;

/**
 * Helper class for handling byte arrays.
 * Please note that, by default, the methods do not perform any sanity checks.
 *
 */
public class ByteArrayHelper {

    public static int getInt32(byte[] array, int index) {
        return array[index+3] & 0xFF |
            (array[index+2] & 0xFF) << 8 |
            (array[index+1] & 0xFF) << 16 |
            (array[index] & 0xFF) << 24;
    }

    public static int getInt32BigEndian(byte[] array, int index) {
        return array[index] & 0xFF |
            (array[index+1] & 0xFF) << 8 |
            (array[index+2] & 0xFF) << 16 |
            (array[index+3] & 0xFF) << 24;
    }

    public static int getInt16(byte[] array, int index) {
        return array[index+1] & 0xFF |
            (array[index] & 0xFF) << 8;
    }

    public static int getInt16BigEndian(byte[] array, int index) {
        return array[index] & 0xFF |
            (array[index+1] & 0xFF) << 8;
    }

    public static int getInt8(byte[] array, int index) {
        return array[index] & 0xFF;
    }

    public static int getInt4L(byte[] array, int index) {
        return array[index] & 0x0F;
    }

    public static int getInt4H(byte[] array, int index) {
        return (array[index] & 0xF0) >> 4;
    }

    public static String getEthMacAddr(byte[] array, int index) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int val = getInt8(array, index + i);

            if (val < 16) {
                sb.append("0");
            }

            sb.append(Integer.toHexString(val).toUpperCase());

            if (i < 5) {
                sb.append(":");
            }
        }

        return sb.toString();
    }
}

