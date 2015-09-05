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

    public static void addToInt(byte[] array, int index, int value) {
        int tmp = array[index+3] & 0xFF |
            (array[index+2] & 0xFF) << 8 |
            (array[index+1] & 0xFF) << 16 |
            (array[index] & 0xFF) << 24;

        tmp += value;

        array[index+3] = (byte) (0xFF & tmp);
        array[index+2] = (byte) ((tmp >> 8) & 0xFF);
        array[index+1] = (byte) ((tmp >> 16) & 0xFF);
        array[index] = (byte) ((tmp >> 24 ) & 0xFF);
    }

    public static int getInt(byte[] array, int index) {
        return array[index+3] & 0xFF |
            (array[index+2] & 0xFF) << 8 |
            (array[index+1] & 0xFF) << 16 |
            (array[index] & 0xFF) << 24;
    }

    public static int getIntBigEndian(byte[] array, int index) {
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

    public static int getByte(byte[] array, int index) {
        return array[index] & 0xFF;
    }

    public static int getNibbleLow(byte[] array, int index) {
        return array[index] & 0x0F;
    }

    public static int getNibbleHigh(byte[] array, int index) {
        return (array[index] & 0xF0) >> 4;
    }

    public static long getLong(byte[] array, int index) {
        return array[index+7] & 0xFF |
            (array[index+6] & 0xFF) << 8 |
            (array[index+5] & 0xFF) << 16 |
            (array[index+4] & 0xFF) << 24 |
            (array[index+3] & 0xFF) << 32 |
            (array[index+2] & 0xFF) << 40 |
            (array[index+1] & 0xFF) << 48 |
            (array[index] & 0xFF) << 56;
    }
}

