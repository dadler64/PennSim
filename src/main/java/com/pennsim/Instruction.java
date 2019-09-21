package com.pennsim;

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Instruction {

    private String originalLine;
    private String format = "";
    private int address;
    private String opcode;
    private String label;
    private String label_ref;
    private Vector regs = new Vector();
    private String stringz;
    private Integer offset_immediate;
    private int line_number = 0;

    Instruction(String var1, int var2) throws AsException {
        this.line_number = var2;
        this.originalLine = var1;
        int var3 = var1.indexOf(59);
        if (var3 != -1) {
            var1 = var1.substring(0, var3);
        }

        var1 = var1.replace("\\\"", "\u0000");
        Matcher var4 = Pattern.compile("([^\"]*)[\"]([^\"]*)[\"](.*)").matcher(var1);
        if (var4.matches()) {
            this.stringz = var4.group(2);
            this.stringz = this.stringz.replace("\u0000", "\"");
            this.stringz = this.stringz.replace("\\n", "\n");
            this.stringz = this.stringz.replace("\\t", "\t");
            this.stringz = this.stringz.replace("\\0", "\u0000");
            var1 = var4.group(1) + " " + var4.group(3);
        }

        var1 = var1.toUpperCase();
        var1 = var1.replace(",", " ");
        var1 = var1.trim();
        if (var1.length() != 0) {
            String[] var5 = var1.split("[\\s]+");

            for (int var6 = 0; var6 < var5.length; ++var6) {
                String var7 = var5[var6];
                if (ISA.isOpcode(var7)) {
                    this.opcode = var7;
                    this.format = this.format + var7 + " ";
                } else if (var7.matches("[#]?[-]?[\\d]+")) {
                    var7 = var7.replace("#", "");
                    this.offset_immediate = Integer.parseInt(var7, 10);
                    this.format = this.format + "Num ";
                } else if (var7.matches("[B][01]+")) {
                    var7 = var7.replace("B", "");
                    this.offset_immediate = Integer.parseInt(var7, 2);
                    this.format = this.format + "Num ";
                } else if (var7.matches("[0]?[X][ABCDEF\\d]+")) {
                    var7 = var7.replace("0X", "");
                    var7 = var7.replace("X", "");
                    this.offset_immediate = Integer.parseInt(var7, 16);
                    this.format = this.format + "Num ";
                } else if (var7.matches("R[\\d]+")) {
                    var7 = var7.replace("R", "");
                    this.regs.add(new Integer(Integer.parseInt(var7, 10)));
                    this.format = this.format + "Reg ";
                } else if (var6 == 0 && var7.matches("[\\w_][\\w_\\d]*[:]?")) {
                    var7 = var7.replace(":", "");
                    this.label = var7;
                } else {
                    if (var6 == 0 || !var7.matches("[\\w_][\\w_\\d]*")) {
                        throw new AsException(this,
                                "Unrecognizable token: `" + var7 + "` on line  " + var2 + "(" + var6
                                        + " " + this.originalLine + ")\n");
                    }

                    this.label_ref = var7;
                    this.format = this.format + "Label ";
                }
            }

            if (this.stringz != null) {
                this.format = this.format + "String";
            }

            this.format = this.format.trim();
            if (this.opcode == null) {
                if (this.format.length() != 0) {
                    throw new AsException(this, "Unexpected instruction format");
                }
            } else {
                ISA.checkFormat(this, this.line_number);
            }

        }
    }

    public String getFormat() {
        return this.format;
    }

    public int getAddress() {
        return this.address;
    }

    public void setAddress(int var1) {
        this.address = var1;
    }

    public String getOriginalLine() {
        return this.originalLine;
    }

    public int getLineNumber() {
        return this.line_number;
    }

    public String getOpcode() {
        return this.opcode;
    }

    public String getLabel() {
        return this.label;
    }

    public String getLabelRef() {
        return this.label_ref;
    }

    public int getRegs(int var1) {
        return (Integer) this.regs.get(var1);
    }

    public String getStringz() {
        return this.stringz;
    }

    public int getOffsetImmediate() throws AsException {
        if (this.offset_immediate == null) {
            throw new AsException(this, "Internal error: no offset/immediate when expected");
        } else {
            return this.offset_immediate;
        }
    }

    public void setOffsetImmediate(int var1) {
        this.offset_immediate = new Integer(var1);
    }

    public void error(String var1) throws AsException {
        throw new AsException(this, var1);
    }

    public void splitLabels(List var1) throws AsException {
        if (this.opcode != null || this.label != null) {
            if (this.opcode != null && this.label != null) {
                var1.add(new Instruction(this.label, this.line_number));
                this.label = null;
                var1.add(this);
            } else {
                var1.add(this);
            }
        }

    }
}
