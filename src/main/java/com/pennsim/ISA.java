package com.pennsim;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class ISA {

    public static InstructionDef[] lookupTable = new InstructionDef[65536];
    public static HashSet opcodeSet = new HashSet();
    public static Hashtable formatToDef = new Hashtable();

    /**
     * @deprecated
     */
    public static void execute(RegisterFile var0, Memory var1, Machine var2)
            throws IllegalMemAccessException, IllegalInstructionException {
        int var3 = var0.getPC();
        var0.checkAddr(var3);
        Word var4 = var1.getInst(var3);
        InstructionDef var5 = lookupTable[var4.getValue()];
        if (var5 == null) {
            throw new IllegalInstructionException("Undefined instruction:  " + var4.toHex());
        } else {
            int var6 = var5.execute(var4, var3, var0, var1, var2);
            var0.setPC(var6);
            ++var2.CYCLE_COUNT;
            ++var2.INSTRUCTION_COUNT;
            int var7 = var2.getBranchPredictor().getPredictedPC(var3);
            if (var6 != var7) {
                var2.CYCLE_COUNT += 2;
                var2.BRANCH_STALL_COUNT += 2;
                var2.getBranchPredictor().update(var3, var6);
            }

            if (var5.isLoad()) {
                Word var8 = var2.getMemory().getInst(var6);
                InstructionDef var9 = lookupTable[var8.getValue()];
                if (var9 == null) {
                    throw new IllegalInstructionException(
                            "Undefined instruction:  " + var8.toHex());
                }

                if (!var9.isStore()) {
                    int var10 = var5.getDestinationReg(var4);
                    if (var10 >= 0 && (var10 == var9.getSourceReg1(var8) || var10 == var9
                            .getSourceReg2(var8))) {
                        ++var2.CYCLE_COUNT;
                        ++var2.LOAD_STALL_COUNT;
                    }
                }
            }

            if (var2.isTraceEnabled()) {
                var2.generateTrace(var5, var3, var4);
            }

        }
    }

    public static String disassemble(Word var0, int var1, Machine var2) {
        if (!var2.lookupAddrToInsn(var1) && !PennSim.isLC3()) {
            return "";
        } else {
            InstructionDef var3 = lookupTable[var0.getValue()];
            return var3 == null ? ".FILL " + var0.toHex() : var3.disassemble(var0, var1, var2);
        }
    }

    public static boolean isOpcode(String var0) {
        return opcodeSet.contains(var0.toUpperCase());
    }

    public static void checkFormat(Instruction var0, int var1) throws AsException {
        if (formatToDef.get(var0.getFormat()) == null) {
            throw new AsException(var0,
                    "Unexpected instruction format: actual: '" + var0.getFormat() + "'");
        }
    }

    public static void encode(Instruction var0, List var1) throws AsException {
        String var2 = var0.getFormat();
        InstructionDef var3 = (InstructionDef) formatToDef.get(var2);
        if (var3 == null) {
            var0.error("Unknown instruction format: " + var2);
        }

    }

    public static boolean isCall(Word var0) throws IllegalInstructionException {
        InstructionDef var1 = lookupTable[var0.getValue()];
        if (var1 != null) {
            return var1.isCall();
        } else {
            throw new IllegalInstructionException("Undefined instruction:  " + var0.toHex());
        }
    }

    public static void createDef(String var0, String var1, InstructionDef var2) {
        var2.setOpcode(var0);
        if (var1 != null) {
            var2.setEncoding(var1);
            if (!var2.isDataDirective()) {
                int var3 = 0;
                int var4 = 0;

                for (int var5 = 0; var5 < 65535; ++var5) {
                    if (var2.match(new Word(var5))) {
                        if (lookupTable[var5] == null) {
                            ++var3;
                            lookupTable[var5] = var2;
                        } else {
                            ++var4;
                        }
                    }
                }

                check(var3 > 0 || var4 > 0,
                        "Useless instruction defined, probably an error, opcode=" + var0);
            }
        }

        formatToDef.put(var2.getFormat(), var2);
        opcodeSet.add(var2.getOpcode().toUpperCase());
    }

    public static void check(boolean var0, String var1) {
        if (!var0) {
            throw new InternalException(var1);
        }
    }

    protected static void labelRefToPCOffset(SymTab var0, Instruction var1, int var2)
            throws AsException {
        int var3 = var1.getAddress() + 1;
        int var4 = var0.lookup(var1.getLabelRef());
        int var5 = var4 - var3;
        if (var4 == -1) {
            throw new AsException(var1, "Undeclared label '" + var1.getLabelRef() + "'");
        } else if (var5 >= -(1 << var2 - 1) && var5 <= 1 << var2 - 1) {
            var1.setOffsetImmediate(var5);
        } else {
            throw new AsException(var1, "Jump offset longer than " + var2 + " bits");
        }
    }

    protected void init() {
        createDef(".ORIG", "xxxx iiiiiiiiiiii", new InstructionDef() {
            public void encode(SymTab var1, Instruction var2, List var3) throws AsException {
                if (var3.size() != 0) {
                    throw new AsException(".ORIG can only appear at the beginning of a file");
                } else {
                    var3.add(new Word(var2.getOffsetImmediate()));
                }
            }

            public boolean isDataDirective() {
                return true;
            }

            public int getNextAddress(Instruction var1) throws AsException {
                return var1.getOffsetImmediate();
            }
        });
        createDef(".FILL", "xxxx iiiiiiiiiiii", new InstructionDef() {
            public void encode(SymTab var1, Instruction var2, List var3) throws AsException {
                var3.add(new Word(var2.getOffsetImmediate()));
            }

            public boolean isDataDirective() {
                return true;
            }
        });
        createDef(".FILL", "xxxx pppppppppppp", new InstructionDef() {
            public void encode(SymTab var1, Instruction var2, List var3) throws AsException {
                int var4 = var1.lookup(var2.getLabelRef());
                if (var4 == -1) {
                    throw new AsException(var2, "Undeclared label: '" + var2.getLabelRef() + "'");
                } else {
                    var3.add(new Word(var4));
                }
            }

            public boolean isDataDirective() {
                return true;
            }
        });
        createDef(".BLKW", "xxxx iiiiiiiiiiii", new InstructionDef() {
            public void encode(SymTab var1, Instruction var2, List var3) throws AsException {
                int var4 = var2.getOffsetImmediate();

                for (int var5 = 0; var5 < var4; ++var5) {
                    var3.add(new Word(0));
                }

            }

            public boolean isDataDirective() {
                return true;
            }

            public int getNextAddress(Instruction var1) throws AsException {
                return var1.getAddress() + var1.getOffsetImmediate();
            }
        });
        createDef(".STRINGZ", "xxxx zzzzzzzzzzzz", new InstructionDef() {
            public void encode(SymTab var1, Instruction var2, List var3) throws AsException {
                for (int var4 = 0; var4 < var2.getStringz().length(); ++var4) {
                    var3.add(new Word(var2.getStringz().charAt(var4)));
                }

                var3.add(new Word(0));
            }

            public boolean isDataDirective() {
                return true;
            }

            public int getNextAddress(Instruction var1) throws AsException {
                return var1.getAddress() + var1.getStringz().length() + 1;
            }
        });
        createDef(".END", "xxxx xxxxxxxxxxxx", new InstructionDef() {
            public void encode(SymTab var1, Instruction var2, List var3) throws AsException {
            }

            public boolean isDataDirective() {
                return true;
            }

            public int getNextAddress(Instruction var1) {
                return var1.getAddress();
            }
        });
    }
}
