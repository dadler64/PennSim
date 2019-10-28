package com.pennsim;

import com.pennsim.exception.AsException;
import com.pennsim.exception.IllegalInstructionException;
import com.pennsim.exception.IllegalMemoryAccessException;
import com.pennsim.exception.InternalException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


public class ISA {

    public static InstructionDefinition[] lookupTable = new InstructionDefinition[Memory.MEM_SIZE];
    public static Hashtable<String, InstructionDefinition> formatToDefinition = new Hashtable<>();
    private static HashSet<String> opcodeSet = new HashSet<>();

    @Deprecated
    public static void execute(RegisterFile registerFile, Memory memory, Machine machine)
            throws IllegalMemoryAccessException, IllegalInstructionException {
        int pc = registerFile.getPC();
        registerFile.checkAddress(pc);
        Word word = memory.getInstruction(pc);
        InstructionDefinition instructionDef = lookupTable[word.getValue()];
        if (instructionDef == null) {
            throw new IllegalInstructionException("Undefined instruction:  " + word.toHex());
        } else {
            int newValue = instructionDef.execute(word, pc, registerFile, memory, machine);
            registerFile.setPC(newValue);
            ++machine.cycleCount;
            ++machine.instructionCount;
            int oldValue = machine.getBranchPredictor().getPredictedPC(pc);
            if (newValue != oldValue) {
                machine.cycleCount += 2;
                machine.branchStallCount += 2;
                machine.getBranchPredictor().update(pc, newValue);
            }

            if (instructionDef.isLoad()) {
                Word loadWord = machine.getMemory().getInstruction(newValue);
                InstructionDefinition loadInstructionDef = lookupTable[loadWord.getValue()];
                if (loadInstructionDef == null) {
                    throw new IllegalInstructionException(
                            "Undefined instruction: " + loadWord.toHex());
                }

                if (!loadInstructionDef.isStore()) {
                    int destinationReg = instructionDef.getDestinationReg(word);
                    if (destinationReg >= 0 && (destinationReg == loadInstructionDef.getSourceReg1(loadWord) ||
                            destinationReg == loadInstructionDef.getSourceReg2(loadWord))) {
                        ++machine.cycleCount;
                        ++machine.loadStallCount;
                    }
                }
            }

            if (machine.isTraceEnabled()) {
                machine.generateTrace(instructionDef, pc, word);
            }

        }
    }

    static String disassemble(Word word, int address, Machine machine) {
        if (!machine.lookupAddressToInstruction(address) && !PennSim.isLC3()) {
            return "";
        } else {
            InstructionDefinition value = lookupTable[word.getValue()];
            return value == null ? ".FILL " + word.toHex() : value.disassemble(word, address, machine);
        }
    }

    static boolean isOpcode(String opcode) {
        return opcodeSet.contains(opcode.toUpperCase());
    }

    static void checkFormat(Instruction instruction, int var1) throws AsException {
        if (formatToDefinition.get(instruction.getFormat()) == null) {
            throw new AsException(instruction,
                    "Unexpected instruction format: actual: '" + instruction.getFormat() + "'");
        }
    }

    public static void encode(Instruction instruction, List var1) throws AsException {
        String instructionFormat = instruction.getFormat();
        InstructionDefinition instructionDef = formatToDefinition.get(instructionFormat);
        if (instructionDef == null) {
            instruction.error("Unknown instruction format: " + instructionFormat);
        }

    }

    static boolean isCall(Word word) throws IllegalInstructionException {
        InstructionDefinition instructionDef = lookupTable[word.getValue()];
        if (instructionDef != null) {
            return instructionDef.isCall();
        } else {
            throw new IllegalInstructionException("Undefined instruction:  " + word.toHex());
        }
    }

    static void createDef(String opcode, String encoding, InstructionDefinition instructionDef) {
        instructionDef.setOpcode(opcode);
        if (encoding != null) {
            instructionDef.setEncoding(encoding);
            if (!instructionDef.isDataDirective()) {
                int var3 = 0;
                int var4 = 0;

                for (int i = 0; i < 65535; ++i) {
                    if (instructionDef.match(new Word(i))) {
                        if (lookupTable[i] == null) {
                            ++var3;
                            lookupTable[i] = instructionDef;
                        } else {
                            ++var4;
                        }
                    }
                }

                check(var3 > 0 || var4 > 0,
                        "Useless instruction defined, probably an error, opcode =" + opcode);
            }
        }

        formatToDefinition.put(instructionDef.getFormat(), instructionDef);
        opcodeSet.add(instructionDef.getOpcode().toUpperCase());
    }

    public static void check(boolean var0, String var1) {
        if (!var0) {
            throw new InternalException(var1);
        }
    }

    public static void labelRefToPCOffset(SymbolTable symbolTable, Instruction instruction, int jumpOffset)
            throws AsException {
        int var3 = instruction.getAddress() + 1;
        int var4 = symbolTable.lookup(instruction.getLabelRef());
        int var5 = var4 - var3;
        if (var4 == -1) {
            throw new AsException(instruction, "Undeclared label '" + instruction.getLabelRef() + "'");
        } else if (var5 >= -(1 << jumpOffset - 1) && var5 <= 1 << jumpOffset - 1) {
            instruction.setOffsetImmediate(var5);
        } else {
            throw new AsException(instruction, "Jump offset longer than " + jumpOffset + " bits");
        }
    }

    /**
     * Initialize the generic opcodes that all instruction sets are based off of
     */
    protected void init() {
        createDef(".ORIG", "xxxx iiiiiiiiiiii", new InstructionDefinition() {
            public void encode(SymbolTable symbolTable, Instruction instruction, List<Word> words) throws AsException {
                if (words.size() != 0) {
                    throw new AsException(".ORIG can only appear at the beginning of a file");
                } else {
                    words.add(new Word(instruction.getOffsetImmediate()));
                }
            }

            public boolean isDataDirective() {
                return true;
            }

            public int getNextAddress(Instruction instruction) throws AsException {
                return instruction.getOffsetImmediate();
            }
        });
        createDef(".FILL", "xxxx iiiiiiiiiiii", new InstructionDefinition() {
            public void encode(SymbolTable symbolTable, Instruction instruction, List<Word> words) throws AsException {
                words.add(new Word(instruction.getOffsetImmediate()));
            }

            public boolean isDataDirective() {
                return true;
            }
        });
        createDef(".FILL", "xxxx pppppppppppp", new InstructionDefinition() {
            public void encode(SymbolTable symbolTable, Instruction instruction, List<Word> words) throws AsException {
                int label = symbolTable.lookup(instruction.getLabelRef());
                if (label == -1) {
                    throw new AsException(
                            instruction, "Undeclared label: '" + instruction.getLabelRef() + "'");
                } else {
                    words.add(new Word(label));
                }
            }

            public boolean isDataDirective() {
                return true;
            }
        });
        createDef(".BLKW", "xxxx iiiiiiiiiiii", new InstructionDefinition() {
            public void encode(SymbolTable symbolTable, Instruction instruction, List<Word> words) throws AsException {
                int offsetImmediate = instruction.getOffsetImmediate();

                for (int i = 0; i < offsetImmediate; ++i) {
                    words.add(new Word(0));
                }

            }

            public boolean isDataDirective() {
                return true;
            }

            public int getNextAddress(Instruction instruction) throws AsException {
                return instruction.getAddress() + instruction.getOffsetImmediate();
            }
        });
        createDef(".STRINGZ", "xxxx zzzzzzzzzzzz", new InstructionDefinition() {
            public void encode(SymbolTable symbolTable, Instruction instruction, List<Word> words) {
                for (int i = 0; i < instruction.getStringz().length(); ++i) {
                    words.add(new Word(instruction.getStringz().charAt(i)));
                }

                words.add(new Word(0));
            }

            public boolean isDataDirective() {
                return true;
            }

            public int getNextAddress(Instruction instruction) {
                return instruction.getAddress() + instruction.getStringz().length() + 1;
            }
        });
        createDef(".END", "xxxx xxxxxxxxxxxx", new InstructionDefinition() {
            public void encode(SymbolTable symbolTable, Instruction instruction, List<Word> words) {
            }

            public boolean isDataDirective() {
                return true;
            }

            public int getNextAddress(Instruction instruction) {
                return instruction.getAddress();
            }
        });
    }
}
