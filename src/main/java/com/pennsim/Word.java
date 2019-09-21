package com.pennsim;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class Word {

    private int value;

    public Word(int value) {
        this.setValue(value);
    }

    public Word() {
        this.value = 0;
    }

    public static String toHex(int var0, boolean var1) {
        String var2 = Integer.toHexString(var0 & '\uffff').toUpperCase();
        if (var2.length() > 4) {
            Console.println("Converting oversized value " + var2 + " to hex.");
        }

        while (var2.length() < 4) {
            var2 = "0" + var2;
        }

        return var1 ? "x" + var2 : var2;
    }

    public static String toHex(int var0) {
        return toHex(var0, true);
    }

    public static String toBinary(int var0, boolean var1) {
        String var2 = Integer.toBinaryString(var0 & '\uffff').toUpperCase();
        if (var2.length() > 16) {
            Console.println("Converting oversized value " + var2 + " to binary.");
        }

        while (var2.length() < 16) {
            var2 = "0" + var2;
        }

        return var1 ? "b" + var2 : var2;
    }

    public static String toBinary(int var0) {
        return toBinary(var0, true);
    }

    public static int parseNum(String var0) {
        int var1;
        try {
            if (var0.indexOf(120) == 0) {
                var1 = Integer.parseInt(var0.replace('x', '0'), 16);
            } else {
                var1 = Integer.parseInt(var0);
            }
        } catch (NumberFormatException | NullPointerException var3) {
            var1 = Integer.MAX_VALUE;
        }

        return var1;
    }

    public static int convertByteArray(byte var0, byte var1) {
        byte var2 = 0;
        short var3 = 255;
        int var4 = var2 | var3 & var0;
        var4 <<= 8;
        var4 |= var3 & var1;
        return var4;
    }

    public void reset() {
        this.value = 0;
    }

    public String toHex() {
        return toHex(this.value, true);
    }

    public String toHex(boolean var1) {
        return toHex(this.value, var1);
    }

    public String toBinary() {
        return toBinary(this.value, true);
    }

    public String toBinary(boolean var1) {
        return toBinary(this.value, var1);
    }

    public String toString() {
        return Integer.toString(this.value);
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int var1) {
        this.value = var1 & '\uffff';
    }

    void writeWordToFile(BufferedOutputStream var1) throws IOException {
        byte var2 = (byte) (this.value >> 8 & 255);
        byte var3 = (byte) (this.value & 255);
        var1.write(var2);
        var1.write(var3);
    }

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

    public int getSext(int var1, int var2) {
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

    public int getBit(int var1) {
        return this.getZext(var1, var1);
    }

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

    public void setSignedField(int var1, int var2, int var3) throws AsException {
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

    public void setUnsignedField(int var1, int var2, int var3) throws AsException {
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
