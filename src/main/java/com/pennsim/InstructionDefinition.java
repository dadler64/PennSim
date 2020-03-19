package com.pennsim;

import com.pennsim.exception.AsException;
import com.pennsim.exception.IllegalInstructionException;
import com.pennsim.exception.IllegalMemoryAccessException;
import com.pennsim.isa.ISA;
import java.util.List;

public abstract class InstructionDefinition {

    private InstructionDefinition.Location dReg = new Location();
    private InstructionDefinition.Location sReg = new Location();
    private InstructionDefinition.Location tReg = new Location();
    private InstructionDefinition.Location signedImmediate = new Location();
    private InstructionDefinition.Location pcOffset = new Location();
    private InstructionDefinition.Location unsignedImmediate = new Location();
    private String opcode = null;
    private String format = "";
    private int mask = 0;
    private int match = 0;

    public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine)
            throws IllegalMemoryAccessException, IllegalInstructionException {
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

    public int getRefAddress(Word word, int position, RegisterFile registerFile, Memory memory)
            throws IllegalMemoryAccessException {
        return 0;
    }

    public final String getOpcode() {
        return this.opcode;
    }

    public final void setOpcode(String opcode) {
        this.opcode = opcode;
    }

    public String getFormat() {
        return (this.opcode.toUpperCase() + " " + this.format).trim();
    }

    public int getNextAddress(Instruction instruction) throws AsException {
        return instruction.getAddress() + 1;
    }

    public int getDestinationReg(Word word) {
        return -1;
    }

    public int getSourceReg1(Word word) {
        return -1;
    }

    public int getSourceReg2(Word word) {
        return -1;
    }

    protected final int getDReg(Word word) {
        ISA.check(this.dReg.valid, "Invalid register");
        return word.getZext(this.dReg.start, this.dReg.end);
    }

    protected final int getSReg(Word word) {
        ISA.check(this.sReg.valid, "Invalid register");
        return word.getZext(this.sReg.start, this.sReg.end);
    }

    protected final int getTReg(Word word) {
        ISA.check(this.tReg.valid, "Invalid register");
        return word.getZext(this.tReg.start, this.tReg.end);
    }

    protected final int getSignedImmediate(Word word) {
        return word.getSext(this.signedImmediate.start, this.signedImmediate.end);
    }

    public final int getPCOffset(Word word) {
        return word.getSext(this.pcOffset.start, this.pcOffset.end);
    }

    private int getUnsignedImmediate(Word word) {
        return word.getZext(this.unsignedImmediate.start, this.unsignedImmediate.end);
    }

    // TODO: Simplify method to decrease "NPath Complexity". Currently has a complexity of 2187.
    public String disassemble(Word word, int address, Machine machine) {
        boolean isMultipleOpcodes = true;
        String opcode = this.getOpcode();
        if (this.dReg.valid) {
            if (isMultipleOpcodes) {
                opcode += " ";
                isMultipleOpcodes = false;
            } else {
                opcode += ", ";
            }

            opcode += "R" + this.getDReg(word);
        }

        if (this.sReg.valid) {
            if (isMultipleOpcodes) {
                opcode += " ";
                isMultipleOpcodes = false;
            } else {
                opcode += ", ";
            }

            opcode += "R" + this.getSReg(word);
        }

        if (this.tReg.valid) {
            if (isMultipleOpcodes) {
                opcode += " ";
                isMultipleOpcodes = false;
            } else {
                opcode += ", ";
            }

            opcode = opcode + "R" + this.getTReg(word);
        }

        if (this.signedImmediate.valid) {
            if (isMultipleOpcodes) {
                opcode += " ";
                isMultipleOpcodes = false;
            } else {
                opcode += ", ";
            }

            opcode += "#" + this.getSignedImmediate(word);
        }

        if (this.pcOffset.valid) {
            if (isMultipleOpcodes) {
                opcode += " ";
                isMultipleOpcodes = false;
            } else {
                opcode += ", ";
            }

            int row = address + this.getPCOffset(word) + 1;
            String symbol = "";
            if (machine != null) {
                symbol = machine.lookupSym(row);
            }

            if (symbol != null) {
                opcode += symbol;
            } else {
                opcode += Word.toHex(row);
            }
        }

        if (this.unsignedImmediate.valid) {
            if (isMultipleOpcodes) {
                opcode += " ";
                isMultipleOpcodes = false;
            } else {
                opcode += ", ";
            }

            opcode += "x" + Integer.toHexString(this.getUnsignedImmediate(word)).toUpperCase();
        }

        return opcode;
    }

    public void encode(SymbolTable symbolTable, Instruction instruction, List<Word> words) throws AsException {
        Word word = new Word();
        word.setValue(this.match);

        int count;
        try {
            count = 0;
            if (this.dReg.valid) {
                word.setUnsignedField(instruction.getRegs(count), this.dReg.start, this.dReg.end);
                ++count;
            }

            if (this.sReg.valid) {
                word.setUnsignedField(instruction.getRegs(count), this.sReg.start, this.sReg.end);
                ++count;
            }

            if (this.tReg.valid) {
                word.setUnsignedField(instruction.getRegs(count), this.tReg.start, this.tReg.end);
                ++count;
            }
        } catch (AsException e) {
            throw new AsException(instruction, "Register number out of range");
        }

        try {
            if (this.signedImmediate.valid) {
                word.setSignedField(instruction.getOffsetImmediate(), this.signedImmediate.start,
                        this.signedImmediate.end);
            }

            if (this.unsignedImmediate.valid) {
                word.setUnsignedField(instruction.getOffsetImmediate(), this.unsignedImmediate.start,
                        this.unsignedImmediate.end);
            }
        } catch (AsException e) {
            throw new AsException(instruction, "Immediate out of range");
        }

        if (this.pcOffset.valid) {
            count = symbolTable.lookup(instruction.getLabelRef());
            if (count == -1) {
                throw new AsException(instruction, "Undeclared label: " + instruction.getLabelRef());
            }

            instruction.setOffsetImmediate(count - (instruction.getAddress() + 1));

            try {
                word.setSignedField(instruction.getOffsetImmediate(), this.pcOffset.start,
                        this.pcOffset.end);
            } catch (AsException e) {
                throw new AsException(instruction, "PC-relative offset out of range");
            }
        }

        words.add(word);
    }


    private String encodeField(String encoding, char encodeChar, String encodingType, InstructionDefinition.Location location) {
        int firstIndex = encoding.indexOf(encodeChar);
        int lastIndex = encoding.lastIndexOf(encodeChar);
        if (firstIndex != -1 && lastIndex != -1) {
            ISA.check(encoding.substring(firstIndex, lastIndex).matches("[" + encodeChar + "]*"),
                    "Strange encoding of '" + encodeChar + "': " + encoding);
            location.valid = true;
            location.start = 15 - firstIndex;
            location.end = 15 - lastIndex;
            this.format = this.format + encodingType + " ";
            return encoding.replaceAll("" + encodeChar, "x");
        } else {
            return encoding;
        }
    }

    public final boolean match(Word word) {
        return (word.getValue() & this.mask) == this.match;
    }

    public final void setEncoding(String encoding) {
        String inputEncoding = encoding;
        encoding = encoding.toLowerCase();
        encoding = encoding.replaceAll("\\s", "");
        encoding = encoding.replaceAll("[^x10iudstpz]", "");
        ISA.check(encoding.length() == 16, "Strange encoding: " + inputEncoding);
        encoding = this.encodeField(encoding, 'd', "Reg", this.dReg);
        encoding = this.encodeField(encoding, 's', "Reg", this.sReg);
        encoding = this.encodeField(encoding, 't', "Reg", this.tReg);
        encoding = this.encodeField(encoding, 'i', "Num", this.signedImmediate);
        encoding = this.encodeField(encoding, 'p', "Label", this.pcOffset);
        encoding = this.encodeField(encoding, 'u', "Num", this.unsignedImmediate);
        encoding = this.encodeField(encoding, 'z', "String", this.unsignedImmediate);
        encoding = encoding.replaceAll("[^x10]", "");
        ISA.check(encoding.length() == 16, "Strange encoding: " + inputEncoding);
        String replacedEncoding = encoding.replaceAll("0", "1");
        replacedEncoding = replacedEncoding.replaceAll("x", "0");
        this.mask = Integer.parseInt(replacedEncoding, 2);
        String var4 = encoding.replaceAll("x", "0");
        this.match = Integer.parseInt(var4, 2);
    }

    static class Location {
        public int start = -1;
        public int end = -1;
        boolean valid = false;
    }
}
