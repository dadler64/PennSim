package com.pennsim.isa;

import com.pennsim.Machine;
import com.pennsim.Memory;
import com.pennsim.RegisterFile;
import com.pennsim.Word;
import com.pennsim.exception.IllegalInstructionException;
import com.pennsim.exception.IllegalMemoryAccessException;

public class LC3 extends ISA {

    public void init() {
        super.init();
        createDef("ADD", "0001 ddd sss 0 00 ttt", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int value = registerFile.getRegister(this.getSReg(word)) + registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), value);
                registerFile.setNZP(value);
                return registerValue + 1;
            }
        });
        createDef("SUB", "0001 ddd sss 0 10 ttt", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int value = registerFile.getRegister(this.getSReg(word)) - registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), value);
                registerFile.setNZP(value);
                return registerValue + 1;
            }
        });
        createDef("ADD", "0001 ddd sss 1 iiiii", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int value = registerFile.getRegister(this.getSReg(word)) + this.getSignedImmediate(word);
                registerFile.setRegister(this.getDReg(word), value);
                registerFile.setNZP(value);
                return registerValue + 1;
            }
        });
        createDef("AND", "0101 ddd sss 0 00 ttt", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int value =
                        registerFile.getRegister(this.getSReg(word)) & registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), value);
                registerFile.setNZP(value);
                return registerValue + 1;
            }
        });
        createDef("AND", "0101 ddd sss 1 iiiii", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int value = registerFile.getRegister(this.getSReg(word)) & this.getSignedImmediate(word);
                registerFile.setRegister(this.getDReg(word), value);
                registerFile.setNZP(value);
                return registerValue + 1;
            }
        });
        createDef("BR", "0000 111 ppppppppp", new BranchDef());
        createDef("BRnzp", "0000 111 ppppppppp", new BranchDef());
        createDef("BRp", "0000 001 ppppppppp", new BranchDef());
        createDef("BRz", "0000 010 ppppppppp", new BranchDef());
        createDef("BRzp", "0000 011 ppppppppp", new BranchDef());
        createDef("BRn", "0000 100 ppppppppp", new BranchDef());
        createDef("BRnp", "0000 101 ppppppppp", new BranchDef());
        createDef("BRnz", "0000 110 ppppppppp", new BranchDef());
        createDef("RET", "1100 000 111 000000", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                return registerFile.getRegister(7);
            }
        });
        createDef("JMP", "1100 000 ddd 000000", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                return registerFile.getRegister(this.getDReg(word));
            }
        });
        createDef("RTT", "1100 000 111 000001", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                registerFile.setPrivMode(false);
                return registerFile.getRegister(7);
            }
        });
        createDef("JMPT", "1100 000 ddd 000001", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                registerFile.setPrivMode(false);
                return registerFile.getRegister(this.getDReg(word));
            }
        });
        createDef("JSR", "0100 1 ppppppppppp", new InstructionDefinition() {
            public boolean isCall() {
                return true;
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                registerFile.setRegister(7, registerValue + 1);
                return registerValue + 1 + this.getPCOffset(word);
            }
        });
        createDef("JSRR", "0100 000 ddd 000000", new InstructionDefinition() {
            public boolean isCall() {
                return true;
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int value = registerFile.getRegister(this.getDReg(word));
                registerFile.setRegister(7, registerValue + 1);
                return value;
            }
        });
        createDef("LD", "0010 ddd ppppppppp", new InstructionDefinition() {
            public boolean isLoad() {
                return true;
            }

            public int getRefAddress(Word word, int position, RegisterFile registerFile, Memory memory) {
                return position + 1 + this.getPCOffset(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine)
                    throws IllegalMemoryAccessException {
                int value = memory.checkAndRead(registerValue + 1 + this.getPCOffset(word)).getValue();
                registerFile.setRegister(this.getDReg(word), value);
                registerFile.setNZP(value);
                return registerValue + 1;
            }
        });
        createDef("LDI", "1010 ddd ppppppppp", new InstructionDefinition() {
            public boolean isLoad() {
                return true;
            }

            public int getRefAddress(Word word, int position, RegisterFile registerFile, Memory memory)
                    throws IllegalMemoryAccessException {
                return memory.checkAndRead(position + 1 + this.getPCOffset(word)).getValue();
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine)
                    throws IllegalMemoryAccessException {
                int value = memory.checkAndRead(registerValue + 1 + this.getPCOffset(word)).getValue();
                int var7 = memory.checkAndRead(value).getValue();
                registerFile.setRegister(this.getDReg(word), var7);
                registerFile.setNZP(var7);
                return registerValue + 1;
            }
        });
        createDef("LDR", "0110 ddd sss iiiiii", new InstructionDefinition() {
            public boolean isLoad() {
                return true;
            }

            public int getRefAddress(Word word, int position, RegisterFile registerFile, Memory memory) {
                return registerFile.getRegister(this.getSReg(word)) + this.getSignedImmediate(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine)
                    throws IllegalMemoryAccessException {
                int value = memory.checkAndRead(
                        registerFile.getRegister(this.getSReg(word)) + this.getSignedImmediate(word))
                        .getValue();
                registerFile.setRegister(this.getDReg(word), value);
                registerFile.setNZP(value);
                return registerValue + 1;
            }
        });
        createDef("LEA", "1110 ddd ppppppppp", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                registerFile.setRegister(this.getDReg(word), registerValue + 1 + this.getPCOffset(word));
                registerFile.setNZP(registerValue + 1 + this.getPCOffset(word));
                return registerValue + 1;
            }
        });
        createDef("ST", "0011 ddd ppppppppp", new InstructionDefinition() {
            public boolean isStore() {
                return true;
            }

            public int getRefAddress(Word word, int position, RegisterFile registerFile, Memory memory) {
                return position + 1 + this.getPCOffset(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine)
                    throws IllegalMemoryAccessException {
                int value = registerFile.getRegister(this.getDReg(word));
                memory.checkAndWrite(registerValue + 1 + this.getPCOffset(word), value);
                return registerValue + 1;
            }
        });
        createDef("STI", "1011 ddd ppppppppp", new InstructionDefinition() {
            public boolean isStore() {
                return true;
            }

            public int getRefAddress(Word word, int position, RegisterFile registerFile, Memory memory)
                    throws IllegalMemoryAccessException {
                return memory.checkAndRead(position + 1 + this.getPCOffset(word)).getValue();
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine)
                    throws IllegalMemoryAccessException {
                int value = memory.checkAndRead(registerValue + 1 + this.getPCOffset(word)).getValue();
                int register = registerFile.getRegister(this.getDReg(word));
                memory.checkAndWrite(value, register);
                return registerValue + 1;
            }
        });
        createDef("STR", "0111 ddd sss iiiiii", new InstructionDefinition() {
            public boolean isStore() {
                return true;
            }

            public int getRefAddress(Word word, int position, RegisterFile registerFile, Memory memory) {
                return registerFile.getRegister(this.getSReg(word)) + this.getSignedImmediate(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine)
                    throws IllegalMemoryAccessException {
                int value = registerFile.getRegister(this.getDReg(word));
                memory.checkAndWrite(registerFile.getRegister(this.getSReg(word)) + this.getSignedImmediate(word),
                        value);
                return registerValue + 1;
            }
        });
        createDef("NOT", "1001 ddd sss 111111", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int value = ~registerFile.getRegister(this.getSReg(word));
                registerFile.setRegister(this.getDReg(word), value);
                registerFile.setNZP(value);
                return registerValue + 1;
            }
        });
        createDef("MUL", "1101 ddd sss 0 00 ttt", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int value = registerFile.getRegister(this.getSReg(word)) * registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), value);
                registerFile.setNZP(value);
                return registerValue + 1;
            }
        });
        createDef("MUL", "1101 ddd sss 1 iiiii", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int value = registerFile.getRegister(this.getSReg(word)) * this.getSignedImmediate(word);
                registerFile.setRegister(this.getDReg(word), value);
                registerFile.setNZP(value);
                return registerValue + 1;
            }
        });
        createDef("RTI", "1000 000000000000", new InstructionDefinition() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine)
                    throws IllegalInstructionException {
                if (registerFile.getPrivMode()) {
                    int register = registerFile.getRegister(6);
                    int value = memory.read(register).getValue();
                    registerFile.setRegister(6, register + 1);
                    int literallyTheSameValue = memory.read(register).getValue();
                    registerFile.setPSR(literallyTheSameValue);
                    return value;
                } else {
                    throw new IllegalInstructionException("RTI can only be executed in privileged mode");
                }
            }
        });
        createDef("GETC", "1111 0000 00100000", new TrapDef());
        createDef("OUT", "1111 0000 00100001", new TrapDef());
        createDef("PUTS", "1111 0000 00100010", new TrapDef());
        createDef("IN", "1111 0000 00100011", new TrapDef());
        createDef("PUTSP", "1111 0000 00100100", new TrapDef());
        createDef("HALT", "1111 0000 00100101", new TrapDef());
        createDef("TRAP", "1111 0000 uuuuuuuu", new TrapDef());
    }

    private static class TrapDef extends InstructionDefinition {

        private TrapDef() { }

        public boolean isCall() {
            return true;
        }

        public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
            registerFile.setPrivMode(true);
            registerFile.setRegister(7, registerValue + 1);
            return memory.read(word.getZext(8, 0)).getValue();
        }
    }

    private static class BranchDef extends InstructionDefinition {

        private BranchDef() { }

        public boolean isBranch() {
            return true;
        }

        public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
            return (word.getBit(11) != 1 || !registerFile.getN()) && (word.getBit(10) != 1 || !registerFile.getZ())
                    && (word.getBit(9) != 1 || !registerFile.getP()) ? registerValue + 1 : registerValue + 1 +
                    this.getPCOffset(word);
        }
    }
}
