package com.pennsim;

import com.pennsim.exception.AsException;
import com.pennsim.isa.ISA;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Instruction {

    private int address;
    private int lineNumber;
    private Integer offsetImmediate;
    private String label;
    private String labelRef;
    private String opcode;
    private String originalLine;
    private String stringz;
    private String format = "";
    private Vector<Integer> regs = new Vector<>();
    private static final char COMMENT_SYMBOL = ';';


    Instruction(String line, int lineNumber) throws AsException {
        this.lineNumber = lineNumber;
        this.originalLine = line;
        int commentIndex = line.indexOf(COMMENT_SYMBOL); // Look for the start of the comment
        if (commentIndex != -1) {
            line = line.substring(0, commentIndex);
        }

        line = line.replace("\\\"", "\u0000");
        Matcher matcher = Pattern.compile("([^\"]*)[\"]([^\"]*)[\"](.*)").matcher(line);
        if (matcher.matches()) {
            this.stringz = matcher.group(2);
            this.stringz = this.stringz.replace("\u0000", "\"");
            this.stringz = this.stringz.replace("\\n", "\n");
            this.stringz = this.stringz.replace("\\t", "\t");
            this.stringz = this.stringz.replace("\\0", "\u0000");
            line = matcher.group(1) + " " + matcher.group(3);
        }

        line = line.toUpperCase();
        line = line.replace(",", " ");
        line = line.trim();
        if (line.length() != 0) {
            String[] parts = line.split("[\\s]+");

            for (int index = 0; index < parts.length; ++index) {
                String token = parts[index];
                if (ISA.isOpcode(token)) {
                    this.opcode = token;
                    this.format += token + " ";
                } else if (token.matches("[#]?[-]?[\\d]+")) {
                    token = token.replace("#", "");
                    this.offsetImmediate = Integer.parseInt(token, 10);
                    this.format += "Num ";
                } else if (token.matches("[B][01]+")) {
                    token = token.replace("B", "");
                    this.offsetImmediate = Integer.parseInt(token, 2);
                    this.format += "Num ";
                } else if (token.matches("[0]?[X][ABCDEF\\d]+")) {
                    token = token.replace("0X", "");
                    token = token.replace("X", "");
                    this.offsetImmediate = Integer.parseInt(token, 16);
                    this.format += "Num ";
                } else if (token.matches("R[\\d]+")) {
                    token = token.replace("R", "");
                    this.regs.add(Integer.parseInt(token, 10));
                    this.format += "Reg ";
                } else if (index == 0 && token.matches("[\\w_][\\w_\\d]*[:]?")) {
                    token = token.replace(":", "");
                    this.label = token;
                } else {
                    if (index == 0 || !token.matches("[\\w_][\\w_\\d]*")) {
                        throw new AsException(this,
                                "Unrecognizable token: `" + token + "` on line  " + lineNumber + "(" + index
                                        + " " + this.originalLine + ")\n");
                    }

                    this.labelRef = token;
                    this.format += "Label ";
                }
            }

            if (this.stringz != null) {
                this.format += "String";
            }

            this.format = this.format.trim();
            if (this.opcode == null) {
                if (this.format.length() != 0) {
                    throw new AsException(this, "Unexpected instruction format");
                }
            } else {
                ISA.checkFormat(this, this.lineNumber);
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
        return this.lineNumber;
    }

    public String getOpcode() {
        return this.opcode;
    }

    public String getLabel() {
        return this.label;
    }

    public String getLabelRef() {
        return this.labelRef;
    }

    int getRegs(int var1) {
        return this.regs.get(var1);
    }

    public String getStringz() {
        return this.stringz;
    }

    public int getOffsetImmediate() throws AsException {
        if (this.offsetImmediate == null) {
            throw new AsException(this, "Internal error: no offset/immediate when expected");
        } else {
            return this.offsetImmediate;
        }
    }

    public void setOffsetImmediate(int offsetImmediate) {
        this.offsetImmediate = offsetImmediate;
    }

    public void error(String message) throws AsException {
        throw new AsException(this, message);
    }

    public void splitLabels(List<Instruction> instructions) throws AsException {
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
