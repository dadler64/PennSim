package com.pennsim;

import com.pennsim.exception.AsException;
import com.pennsim.exception.InternalException;
import com.pennsim.gui.Console;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * This class was named 'Word' after the 2 Byte data type that exists in C/C++ and other
 * low level programming languages.
 *
 * Functionality wise this class holds a value and is also  designed to be able to manipulate
 * that value as it is used throughout this project.
 */
public class Word {

    private int value;

    public Word(int value) {
        this.setValue(value);
    }

    public Word() {
        this.value = 0;
    }

    /**
     * Get the hex value of an integer
     *
     * @param value the number to be converted
     * @param showPrefix option for whether or not to return the prefix of 'x'
     * @return the hex value as a String
     */
    static String toHex(int value, boolean showPrefix) {
        StringBuilder builder = new StringBuilder(Integer.toHexString(value & '\uffff').toUpperCase());
        if (builder.length() > 4) {
            Console.println("Converting oversized value " + builder + " to hex.");
        }

        while (builder.length() < 4) {
            builder.insert(0, "0");
        }

        return showPrefix ? "x" + builder : builder.toString();
    }

    /**
     * Get the hex value of an integer with the appropriate prefix
     *
     * @param value the number to be converted
     * @return the hex value as a String
     */
    public static String toHex(int value) {
        return toHex(value, true);
    }

    /**
     * Get the binary value of an integer
     *
     * @param value the number to be converted
     * @param showPrefix option for whether or not to return the prefix of 'b'
     * @return the binary value as a String
     */
    private static String toBinary(int value, boolean showPrefix) {
        StringBuilder builder = new StringBuilder(
                Integer.toBinaryString(value & '\uffff').toUpperCase());
        if (builder.length() > 16) {
            Console.println("Converting oversized value " + builder + " to binary.");
        }

        while (builder.length() < 16) {
            builder.insert(0, "0");
        }

        return showPrefix ? "b" + builder : builder.toString();
    }

    /**
     * Get the binary value of an integer with the appropriate prefix
     *
     * @param value the number to be converted
     * @return the binary value as a String
     */
    public static String toBinary(int value) {
        return toBinary(value, true);
    }

    /**
     * Function to convert binary or hex values to their appropriate Integer values
     *
     * @param value the value to be converted to an Integer
     * @return the converted value as an Integer
     */
    static int parseNum(String value) {
        int number;
        try {
            if (value.indexOf(120) == 0) {
                number = Integer.parseInt(value.replace('x', '0'), 16);
            } else {
                number = Integer.parseInt(value);
            }
        } catch (NumberFormatException | NullPointerException e) {
            number = Integer.MAX_VALUE;
        }

        return number;
    }

    /**
     * TODO: Study this function
     */
    static int convertByteArray(byte var0, byte var1) {
        byte var2 = 0;
        short var3 = 255;
        int var4 = var2 | var3 & var0;
        var4 <<= 8;
        var4 |= var3 & var1;
        return var4;
    }

    /**
     * reset the value of this object to '0'
     */
    public void reset() {
        this.value = 0;
    }

    /**
     * Convert the value of this object to hex
     *
     * @return the value of this object to hex
     */
    public String toHex() {
        return toHex(this.value, true);
    }


    /**
     * Convert the value of this object to hex with the option to include the prefix
     *
     * @return the value of this object to hex
     */
    String toHex(boolean showPrefix) {
        return toHex(this.value, showPrefix);
    }


    /**
     * Convert the value of this object to binary
     *
     * @return the value of this object to binary
     */
    String toBinary() {
        return toBinary(this.value, true);
    }


    /**
     * Convert the value of this object to binary with the option to include the prefix
     *
     * @return the value of this object to binary
     */
    public String toBinary(boolean showPrefix) {
        return toBinary(this.value, showPrefix);
    }

    /**
     * Get the value of this object as a string
     *
     * @return the value of this object as a string
     */
    public String toString() {
        return Integer.toString(this.value);
    }

    /**
     * Get the value of this object
     *
     * @return the value of this object
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Set the value of this object
     *
     * @param value the value of this object
     */
    void setValue(int value) {
        this.value = value & '\uffff';
    }

    /**
     * Function to write the binary value of each word the the file
     */
    public void writeWordToFile(BufferedOutputStream stream) throws IOException {
        byte var2 = (byte) (this.value >> 8 & 255);
        byte var3 = (byte) (this.value & 255);
        stream.write(var2);
        stream.write(var3);
    }

    /**
     * Get the Z-Extention of two values
     */
    public int getZext(int var1, int var2) {
        int var3 = this.value;
        if (var2 > var1) {
            return this.getZext(var2, var1);
        } else if (var1 <= 15 && var1 >= 0 && var2 <= 15 && var2 >= 0) {
            var3 &= ~(-1 << var1 + 1);
            var3 >>= var2;
            return var3;
        } else {
            throw new InternalException("Bits out of range: " + var1 + " " + var2);
        }
    }

    /**
     * Get the S-Extention of two values
     */
    int getSext(int var1, int var2) {
        int var3 = this.value;
        if (var2 > var1) {
            return this.getSext(var2, var1);
        } else if (var1 <= 15 && var1 >= 0 && var2 <= 15 && var2 >= 0) {
            int var4 = var3 & 1 << var1;
            if (var4 != 0) {
                var3 |= -1 << var1;
            } else {
                var3 &= ~(-1 << var1 + 1);
            }

            var3 >>= var2;
            return var3;
        } else {
            throw new InternalException("Bits out of range: " + var1 + " " + var2);
        }
    }

    /**
     * TODO: Study this function
     */
    public int getBit(int value) {
        return this.getZext(value, value);
    }

    /**
     * TODO: Study this function
     */
    private void setField(int var1, int var2, int var3) throws AsException {
        if (var3 > var2) {
            throw new AsException("Hi and lo bit operands reversed.");
        } else if (var2 <= 15 && var2 >= 0 && var3 <= 15 && var3 >= 0) {
            byte var4 = -1;
            int var5 = var4 << var2 - var3 + 1;
            var5 = ~var5;
            var5 <<= var3;
            this.value = var5 & var1 << var3 | ~var5 & this.value;
        } else {
            throw new AsException("Bits out of range: " + var2 + " " + var3);
        }
    }

    /**
     * TODO: Study this function
     */
    void setSignedField(int var1, int var2, int var3) throws AsException {
        if (var3 > var2) {
            throw new AsException("Hi and lo bit operands reversed.");
        } else if (var2 <= 15 && var2 >= 0 && var3 <= 15 && var3 >= 0) {
            int var4 = var1 >> var2 - var3;
            if (var4 != 0 && var4 != -1) {
                throw new AsException("Immediate out of range: " + var1);
            } else {
                this.setField(var1, var2, var3);
                this.setField(var1, var2, var3);
            }
        } else {
            throw new InternalException("Bits out of range: " + var2 + " " + var3);
        }
    }

    /**
     * TODO: Study this function
     */
    void setUnsignedField(int var1, int var2, int var3) throws AsException {
        if (var3 > var2) {
            throw new AsException("Hi and lo bit operands reversed.");
        } else if (var2 <= 15 && var2 >= 0 && var3 <= 15 && var3 >= 0) {
            int var4 = var1 >> var2 - var3 + 1;
            if (var4 == 0) {
                this.setField(var1, var2, var3);
            } else {
                throw new AsException("Immediate out of range: " + var1);
            }
        } else {
            throw new InternalException("Bits out of range: " + var2 + " " + var3);
        }
    }
}
