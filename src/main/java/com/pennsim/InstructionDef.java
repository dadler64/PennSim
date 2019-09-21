package com.pennsim;

import java.util.List;

public abstract class InstructionDef {

    InstructionDef.Location dReg = new InstructionDef.Location();
    InstructionDef.Location sReg = new InstructionDef.Location();
    InstructionDef.Location tReg = new InstructionDef.Location();
    InstructionDef.Location signedImmed = new InstructionDef.Location();
    InstructionDef.Location pcOffset = new InstructionDef.Location();
    InstructionDef.Location unsignedImmed = new InstructionDef.Location();
    private String opcode = null;
    private String format = "";
    private int mask = 0;
    private int match = 0;

    public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5)
            throws IllegalMemAccessException, IllegalInstructionException {
        throw new IllegalInstructionException("Abstract instruction (or pseudo-instruction)");
    }

    public boolean isDataDirective() {
        return false;
    }

    public boolean isCall() {
        return false;
    }

    public boolean isBranch() {
        return false;
    }

    public boolean isLoad() {
        return false;
    }

    public boolean isStore() {
        return false;
    }

    public int getRefAddr(Word var1, int var2, RegisterFile var3, Memory var4)
            throws IllegalMemAccessException {
        return 0;
    }

    public final String getOpcode() {
        return this.opcode;
    }

    public final void setOpcode(String var1) {
        this.opcode = var1;
    }

    public String getFormat() {
        return (this.opcode.toUpperCase() + " " + this.format).trim();
    }

    public int getNextAddress(Instruction var1) throws AsException {
        return var1.getAddress() + 1;
    }

    public int getDestinationReg(Word var1) {
        return -1;
    }

    public int getSourceReg1(Word var1) {
        return -1;
    }

    public int getSourceReg2(Word var1) {
        return -1;
    }

    public final int getDReg(Word var1) {
        ISA.check(this.dReg.valid, "Invalid register");
        return var1.getZext(this.dReg.start, this.dReg.end);
    }

    public final int getSReg(Word var1) {
        ISA.check(this.sReg.valid, "Invalid register");
        return var1.getZext(this.sReg.start, this.sReg.end);
    }

    public final int getTReg(Word var1) {
        ISA.check(this.tReg.valid, "Invalid register");
        return var1.getZext(this.tReg.start, this.tReg.end);
    }

    public final int getSignedImmed(Word var1) {
        return var1.getSext(this.signedImmed.start, this.signedImmed.end);
    }

    public final int getPCOffset(Word var1) {
        return var1.getSext(this.pcOffset.start, this.pcOffset.end);
    }

    public final int getUnsignedImmed(Word var1) {
        return var1.getZext(this.unsignedImmed.start, this.unsignedImmed.end);
    }

    public String disassemble(Word var1, int var2, Machine var3) {
        boolean var4 = true;
        String var5 = this.getOpcode();
        if (this.dReg.valid) {
            if (var4) {
                var5 = var5 + " ";
                var4 = false;
            } else {
                var5 = var5 + ", ";
            }

            var5 = var5 + "R" + this.getDReg(var1);
        }

        if (this.sReg.valid) {
            if (var4) {
                var5 = var5 + " ";
                var4 = false;
            } else {
                var5 = var5 + ", ";
            }

            var5 = var5 + "R" + this.getSReg(var1);
        }

        if (this.tReg.valid) {
            if (var4) {
                var5 = var5 + " ";
                var4 = false;
            } else {
                var5 = var5 + ", ";
            }

            var5 = var5 + "R" + this.getTReg(var1);
        }

        if (this.signedImmed.valid) {
            if (var4) {
                var5 = var5 + " ";
                var4 = false;
            } else {
                var5 = var5 + ", ";
            }

            var5 = var5 + "#" + this.getSignedImmed(var1);
        }

        if (this.pcOffset.valid) {
            if (var4) {
                var5 = var5 + " ";
                var4 = false;
            } else {
                var5 = var5 + ", ";
            }

            int var6 = var2 + this.getPCOffset(var1) + 1;
            String var7 = null;
            if (var3 != null) {
                var7 = var3.lookupSym(var6);
            }

            if (var7 != null) {
                var5 = var5 + var7;
            } else {
                var5 = var5 + Word.toHex(var6);
            }
        }

        if (this.unsignedImmed.valid) {
            if (var4) {
                var5 = var5 + " ";
                var4 = false;
            } else {
                var5 = var5 + ", ";
            }

            var5 = var5 + "x" + Integer.toHexString(this.getUnsignedImmed(var1)).toUpperCase();
        }

        return var5;
    }

    public void encode(SymTab var1, Instruction var2, List var3) throws AsException {
        Word var4 = new Word();
        var4.setValue(this.match);

        int var5;
        try {
            var5 = 0;
            if (this.dReg.valid) {
                var4.setUnsignedField(var2.getRegs(var5), this.dReg.start, this.dReg.end);
                ++var5;
            }

            if (this.sReg.valid) {
                var4.setUnsignedField(var2.getRegs(var5), this.sReg.start, this.sReg.end);
                ++var5;
            }

            if (this.tReg.valid) {
                var4.setUnsignedField(var2.getRegs(var5), this.tReg.start, this.tReg.end);
                ++var5;
            }
        } catch (AsException var9) {
            throw new AsException(var2, "Register number out of range");
        }

        try {
            if (this.signedImmed.valid) {
                var4.setSignedField(var2.getOffsetImmediate(), this.signedImmed.start,
                        this.signedImmed.end);
            }

            if (this.unsignedImmed.valid) {
                var4.setUnsignedField(var2.getOffsetImmediate(), this.unsignedImmed.start,
                        this.unsignedImmed.end);
            }
        } catch (AsException var8) {
            throw new AsException(var2, "Immediate out of range");
        }

        if (this.pcOffset.valid) {
            var5 = var1.lookup(var2.getLabelRef());
            if (var5 == -1) {
                throw new AsException(var2, "Undeclared label: " + var2.getLabelRef());
            }

            var2.setOffsetImmediate(var5 - (var2.getAddress() + 1));

            try {
                var4.setSignedField(var2.getOffsetImmediate(), this.pcOffset.start,
                        this.pcOffset.end);
            } catch (AsException var7) {
                throw new AsException(var2, "PC-relative offset out of range");
            }
        }

        var3.add(var4);
    }

    private String encodeField(String var1, char var2, String var3, InstructionDef.Location var4) {
        int var5 = var1.indexOf(var2);
        int var6 = var1.lastIndexOf(var2);
        if (var5 != -1 && var6 != -1) {
            ISA.check(var1.substring(var5, var6).matches("[" + var2 + "]*"),
                    "Strange encoding of '" + var2 + "': " + var1);
            var4.valid = true;
            var4.start = 15 - var5;
            var4.end = 15 - var6;
            this.format = this.format + var3 + " ";
            return var1.replaceAll("" + var2, "x");
        } else {
            return var1;
        }
    }

    public final boolean match(Word var1) {
        return (var1.getValue() & this.mask) == this.match;
    }

    public final void setEncoding(String var1) {
        String var2 = var1;
        var1 = var1.toLowerCase();
        var1 = var1.replaceAll("\\s", "");
        var1 = var1.replaceAll("[^x10iudstpz]", "");
        ISA.check(var1.length() == 16, "Strange encoding: " + var2);
        var1 = this.encodeField(var1, 'd', "Reg", this.dReg);
        var1 = this.encodeField(var1, 's', "Reg", this.sReg);
        var1 = this.encodeField(var1, 't', "Reg", this.tReg);
        var1 = this.encodeField(var1, 'i', "Num", this.signedImmed);
        var1 = this.encodeField(var1, 'p', "Label", this.pcOffset);
        var1 = this.encodeField(var1, 'u', "Num", this.unsignedImmed);
        var1 = this.encodeField(var1, 'z', "String", this.unsignedImmed);
        var1 = var1.replaceAll("[^x10]", "");
        ISA.check(var1.length() == 16, "Strange encoding: " + var2);
        String var3 = var1.replaceAll("0", "1");
        var3 = var3.replaceAll("x", "0");
        this.mask = Integer.parseInt(var3, 2);
        String var4 = var1.replaceAll("x", "0");
        this.match = Integer.parseInt(var4, 2);
    }

    class Location {

        public boolean valid = false;
        public int start = -1;
        public int end = -1;
    }
}
