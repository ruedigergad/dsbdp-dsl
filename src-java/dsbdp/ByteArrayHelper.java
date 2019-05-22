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

    public static long getInt64(byte[] array, int index) {
        return array[index+7] & 0xFFL |
            (array[index+6] & 0xFFL) << 8 |
            (array[index+5] & 0xFFL) << 16 |
            (array[index+4] & 0xFFL) << 24 |
            (array[index+3] & 0xFFL) << 32 |
            (array[index+2] & 0xFFL) << 40 |
            (array[index+1] & 0xFFL) << 48 |
            (array[index] & 0xFFL) << 56;
    }

    public static long getInt64BigEndian(byte[] array, int index) {
        return array[index] & 0xFFL |
            (array[index+1] & 0xFFL) << 8 |
            (array[index+2] & 0xFFL) << 16 |
            (array[index+3] & 0xFFL) << 24 |
            (array[index+4] & 0xFFL) << 32 |
            (array[index+5] & 0xFFL) << 40 |
            (array[index+6] & 0xFFL) << 48 |
            (array[index+7] & 0xFFL) << 56;
    }

    public static long getInt32(byte[] array, int index) {
        return array[index+3] & 0xFFL |
            (array[index+2] & 0xFFL) << 8 |
            (array[index+1] & 0xFFL) << 16 |
            (array[index] & 0xFFL) << 24;
    }

    public static long getInt32BigEndian(byte[] array, int index) {
        return array[index] & 0xFFL |
            (array[index+1] & 0xFFL) << 8 |
            (array[index+2] & 0xFFL) << 16 |
            (array[index+3] & 0xFFL) << 24;
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

    public static int getInt8(byte[] array, int indexBytes, int indexBits) {
        int additionalByteShift = indexBits / 8;
        int bitShifts = indexBits % 8;

        if (bitShifts == 0) {
            return array[additionalByteShift];
        }

        int lowerMask = ((int) Math.pow(2, 8 - bitShifts)) - 1;
        int lowerByte = (array[indexBytes + additionalByteShift] >> bitShifts) & lowerMask;

        int upperMask = ((int) Math.pow(2, bitShifts)) - 1;
        int upperByte = array[indexBytes + additionalByteShift + 1] & upperMask;

        return lowerByte + ((int) Math.pow(2, 8 - bitShifts)) * upperByte;
    }

    public static int getInt8x(byte[] array, int indexBytes, int indexBits, int length) {
        int additionalByteShift = indexBits / 8;
        int byteShifts = indexBytes + additionalByteShift;
        int bitShifts = indexBits % 8;

        if (bitShifts == 0) {
            return array[byteShifts] & (0xFF >> (8 - length));
        }

        if (bitShifts + length <= 8) {
            int mask = (int) Math.pow(2, length) - 1;
            return (array[byteShifts] >> bitShifts) & mask;
        }

        int lowerMask = ((int) Math.pow(2, 8 - bitShifts)) - 1;
        int lowerByte = (array[byteShifts] >> bitShifts) & lowerMask;

        int upperMask = ((int) Math.pow(2, length - (8 - bitShifts))) - 1;
        int upperByte = array[byteShifts + 1] & upperMask;

        return lowerByte + ((int) Math.pow(2, 8 - bitShifts)) * upperByte;
    }

    public static int getInt4L(byte[] array, int index) {
        return array[index] & 0x0F;
    }

    public static int getInt4H(byte[] array, int index) {
        return (array[index] & 0xF0) >> 4;
    }

    public static String getEthMacAddrString(byte[] array, int index) {
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

    public static String getIpv4AddrString(byte[] array, int index) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            sb.append(getInt8(array, index + i));

            if (i < 3) {
                sb.append(".");
            }
        }

        return sb.toString();
    }

    public static String byteArrayToString(byte[] array, int index, int length) {
        return new String(array, index, length);
    }
}

