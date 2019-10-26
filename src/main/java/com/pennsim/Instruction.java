package com.pennsim;

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Instruction {

    private int address;
    private int lineNumber;
    private Integer offsetImmediate;
    private String label;
    private String labelRef;
    private String opcode;
    private String originalLine;
    private String stringz;
    private StringBuilder builder = new StringBuilder();
    private Vector<Integer> regs = new Vector<>();

    Instruction(String label, int lineNumber) throws AsException {
        this.lineNumber = lineNumber;
        this.originalLine = label;
        int var3 = label.indexOf(59);
        if (var3 != -1) {
            label = label.substring(0, var3);
        }

        label = label.replace("\\\"", "\u0000");
        Matcher matcher = Pattern.compile("([^\"]*)[\"]([^\"]*)[\"](.*)").matcher(label);
        if (matcher.matches()) {
            this.stringz = matcher.group(2);
            this.stringz = this.stringz.replace("\u0000", "\"");
            this.stringz = this.stringz.replace("\\n", "\n");
            this.stringz = this.stringz.replace("\\t", "\t");
            this.stringz = this.stringz.replace("\\0", "\u0000");
            label = matcher.group(1) + " " + matcher.group(3);
        }

        label = label.toUpperCase();
        label = label.replace(",", " ");
        label = label.trim();
        if (label.length() != 0) {
            String[] words = label.split("[\\s]+");

            for (int i = 0; i < words.length; ++i) {
                String token = words[i];
                if (ISA.isOpcode(token)) {
                    this.opcode = token;
                    this.builder.append(token);
                    this.builder.append(" ");
                    // For a numerical
                } else if (token.matches("[#]?[-]?[\\d]+")) {
                    token = token.replace("#", "");
                    this.offsetImmediate = Integer.parseInt(token, 10);
                    this.builder.append("Num ");
                    // For binary values
                } else if (token.matches("[B][01]+")) {
                    token = token.replace("B", "");
                    this.offsetImmediate = Integer.parseInt(token, 2);
                    this.builder.append("Num ");
                    // For hexadecimal values
                } else if (token.matches("[0]?[X][ABCDEF\\d]+")) {
                    token = token.replace("0X", "");
                    token = token.replace("X", "");
                    this.offsetImmediate = Integer.parseInt(token, 16);
                    this.builder.append("Num ");
                } else if (token.matches("R[\\d]+")) {
                    token = token.replace("R", "");
                    this.regs.add(Integer.parseInt(token, 10));
                    this.builder.append("Reg ");
                } else if (i == 0 && token.matches("[\\w_][\\w_\\d]*[:]?")) {
                    token = token.replace(":", "");
                    this.label = token;
                } else {
                    if (i == 0 || !token.matches("[\\w_][\\w_\\d]*")) {
                        throw new AsException(this,
                                "Unrecognizable token: `" + token + "` on line  " + lineNumber + "(" + i
                                        + " " + this.originalLine + ")\n");
                    }

                    this.labelRef = token;
                    this.builder.append("Label ");
                }
            }

            if (this.stringz != null) {
                this.builder.append("String");
            }

            String format = this.builder.toString();
            format = format.trim();
            if (this.opcode == null) {
                if (format.length() != 0) {
                    throw new AsException(this, "Unexpected instruction format");
                }
            } else {
                ISA.checkFormat(this, this.lineNumber);
            }

        }
    }

    String getFormat() {
        return this.builder.toString();
    }

    public int getAddress() {
        return this.address;
    }

    public void setAddress(int var1) {
        this.address = var1;
    }

    String getOriginalLine() {
        return this.originalLine;
    }

    int getLineNumber() {
        return this.lineNumber;
    }

    String getOpcode() {
        return this.opcode;
    }

    public String getLabel() {
        return this.label;
    }

    String getLabelRef() {
        return this.labelRef;
    }

    int getRegs(int var1) {
        return this.regs.get(var1);
    }

    String getStringz() {
        return this.stringz;
    }

    int getOffsetImmediate() throws AsException {
        if (this.offsetImmediate == null) {
            throw new AsException(this, "Internal error: no offset/immediate when expected");
        } else {
            return this.offsetImmediate;
        }
    }

    void setOffsetImmediate(int offsetImmediate) {
        this.offsetImmediate = offsetImmediate;
    }

    /**
     * Throw an assembly error
     *
     * @param message the error message to pass along
     */
    public void error(String message) throws AsException {
        throw new AsException(this, message);
    }

    void splitLabels(List<Instruction> instructions) throws AsException {
        if (this.opcode != null || this.label != null) {
            if (this.opcode != null && this.label != null) {
                instructions.add(new Instruction(this.label, this.lineNumber));
                this.label = null;
                instructions.add(this);
            } else {
                instructions.add(this);
            }
        }

    }
}
