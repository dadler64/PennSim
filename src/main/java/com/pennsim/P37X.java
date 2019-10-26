package com.pennsim;

public class P37X extends ISA {

    public void init() {
        super.init();
        createDef("ADD", "0000 ddd sss ttt 100", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg1(Word word) {
                return this.getSReg(word);
            }

            public int getSourceReg2(Word word) {
                return this.getTReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 =
                        registerFile.getRegister(this.getSReg(word)) + registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("SUB", "0000 ddd sss ttt 101", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg1(Word word) {
                return this.getSReg(word);
            }

            public int getSourceReg2(Word word) {
                return this.getTReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 =
                        registerFile.getRegister(this.getSReg(word)) - registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("MUL", "0000 ddd sss ttt 110", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg1(Word word) {
                return this.getSReg(word);
            }

            public int getSourceReg2(Word word) {
                return this.getTReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 =
                        registerFile.getRegister(this.getSReg(word)) * registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("OR", "0001 ddd sss ttt 000", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg1(Word word) {
                return this.getSReg(word);
            }

            public int getSourceReg2(Word word) {
                return this.getTReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 =
                        registerFile.getRegister(this.getSReg(word)) | registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("NOT", "0001 ddd sss xxx 001", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg1(Word word) {
                return this.getSReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 = ~registerFile.getRegister(this.getSReg(word));
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("AND", "0001 ddd sss ttt 010", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg1(Word word) {
                return this.getSReg(word);
            }

            public int getSourceReg2(Word word) {
                return this.getTReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 =
                        registerFile.getRegister(this.getSReg(word)) & registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("XOR", "0001 ddd sss ttt 011", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg1(Word word) {
                return this.getSReg(word);
            }

            public int getSourceReg2(Word word) {
                return this.getTReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 =
                        registerFile.getRegister(this.getSReg(word)) ^ registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("SLL", "0001 ddd sss ttt 100", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg1(Word word) {
                return this.getSReg(word);
            }

            public int getSourceReg2(Word word) {
                return this.getTReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 = registerFile.getRegister(this.getSReg(word)) << (
                        registerFile.getRegister(this.getTReg(word)) & 15);
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("SRL", "0001 ddd sss ttt 101", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg1(Word word) {
                return this.getSReg(word);
            }

            public int getSourceReg2(Word word) {
                return this.getTReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 = registerFile.getRegister(this.getSReg(word)) >>> (
                        registerFile.getRegister(this.getTReg(word)) & 15);
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("SRA", "0001 ddd sss ttt 110", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg1(Word word) {
                return this.getSReg(word);
            }

            public int getSourceReg2(Word word) {
                return this.getTReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 = (short) registerFile.getRegister(this.getSReg(word)) >> (
                        registerFile.getRegister(this.getTReg(word)) & 15);
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("GETC", "0010 0000 00100000", new P37X.TrapDef());
        createDef("PUTC", "0010 0000 00100001", new P37X.TrapDef());
        createDef("PUTS", "0010 0000 00100010", new P37X.TrapDef());
        createDef("EGETC", "0010 0000 00100011", new P37X.TrapDef());
        createDef("HALT", "0010 0000 00100101", new P37X.TrapDef());
        createDef("TRAP", "0010 0000 uuuuuuuu", new P37X.TrapDef());
        createDef("RTT", "0011 ddd xxxxxxxxx", new InstructionDef() {
            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                registerFile.setPrivMode(false);
                return registerFile.getRegister(this.getDReg(word));
            }
        });
        createDef("JUMP", "0100 pppppppppppp", new InstructionDef() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                return registerValue + 1 + this.getPCOffset(word);
            }
        });
        createDef("JUMPR", "0101 ddd xxxxxxxxx", new InstructionDef() {
            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                return registerFile.getRegister(this.getDReg(word));
            }
        });
        createDef("JSR", "0110 pppppppppppp", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return 7;
            }

            public boolean isCall() {
                return true;
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                registerFile.setRegister(7, registerValue + 1);
                return registerValue + 1 + this.getPCOffset(word);
            }
        });
        createDef("JSRR", "0111 ddd xxxxxxxxx", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return 7;
            }

            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public boolean isCall() {
                return true;
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 = registerFile.getRegister(this.getDReg(word));
                registerFile.setRegister(7, registerValue + 1);
                return var6;
            }
        });
        createDef("NOOP", "1000 xxx 000 xxxxxx", new InstructionDef() {
            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                return registerValue + 1;
            }
        });
        createDef("BRP", "1000 ddd 001 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 = registerFile.getRegister(this.getDReg(word)) & '\uffff';
                return var6 != 0 && (var6 & 32768) == 0 ? registerValue + 1 + this.getPCOffset(word)
                        : registerValue + 1;
            }
        });
        createDef("BRZ", "1000 ddd 010 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 = registerFile.getRegister(this.getDReg(word)) & '\uffff';
                return var6 == 0 ? registerValue + 1 + this.getPCOffset(word) : registerValue + 1;
            }
        });
        createDef("BRZP", "1000 ddd 011 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 = registerFile.getRegister(this.getDReg(word)) & '\uffff';
                return var6 != 0 && (var6 & 32768) != 0 ? registerValue + 1
                        : registerValue + 1 + this.getPCOffset(word);
            }
        });
        createDef("BRN", "1000 ddd 100 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 = registerFile.getRegister(this.getDReg(word)) & '\uffff';
                return (var6 & 32768) != 0 ? registerValue + 1 + this.getPCOffset(word) : registerValue
                        + 1;
            }
        });
        createDef("BRNP", "1000 ddd 101 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 = registerFile.getRegister(this.getDReg(word)) & '\uffff';
                return var6 != 0 ? registerValue + 1 + this.getPCOffset(word) : registerValue + 1;
            }
        });
        createDef("BRNZ", "1000 ddd 110 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 = registerFile.getRegister(this.getDReg(word)) & '\uffff';
                return var6 != 0 && (var6 & 32768) == 0 ? registerValue + 1
                        : registerValue + 1 + this.getPCOffset(word);
            }
        });
        createDef("BRNZP", "1000 ddd 111 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                return registerValue + 1 + this.getPCOffset(word);
            }
        });
        createDef("CONST", "1001 ddd iiiiiiiii", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                registerFile.setRegister(this.getDReg(word), this.getSignedImmediate(word));
                return registerValue + 1;
            }
        });
        createDef("INC", "1010 ddd iiiiiiiii", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                int var6 = registerFile.getRegister(this.getDReg(word)) + this.getSignedImmediate(word);
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("LEA", "1011 ddd ppppppppp", new InstructionDef() {
            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
                registerFile.setRegister(this.getDReg(word), registerValue + 1 + this.getPCOffset(word));
                return registerValue + 1;
            }
        });
        createDef("LDR", "1100 ddd sss iiiiii", new InstructionDef() {
            public boolean isLoad() {
                return true;
            }

            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg1(Word word) {
                return this.getSReg(word);
            }

            public int getRefAddress(Word word, int position, RegisterFile registerFile, Memory memory)
                    throws IllegalMemoryAccessException {
                return registerFile.getRegister(this.getSReg(word)) + this.getSignedImmediate(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine)
                    throws IllegalMemoryAccessException {
                int var6 = memory.checkAndRead(
                        registerFile.getRegister(this.getSReg(word)) + this.getSignedImmediate(word))
                        .getValue();
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("STR", "1101 ddd sss iiiiii", new InstructionDef() {
            public boolean isStore() {
                return true;
            }

            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public int getSourceReg2(Word word) {
                return this.getSReg(word);
            }

            public int getRefAddress(Word word, int position, RegisterFile registerFile, Memory memory)
                    throws IllegalMemoryAccessException {
                return registerFile.getRegister(this.getSReg(word)) + this.getSignedImmediate(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine)
                    throws IllegalMemoryAccessException {
                int var6 = registerFile.getRegister(this.getDReg(word));
                memory.checkAndWrite(registerFile.getRegister(this.getSReg(word)) + this.getSignedImmediate(word),
                        var6);
                return registerValue + 1;
            }
        });
        createDef("LD", "1110 ddd ppppppppp", new InstructionDef() {
            public boolean isLoad() {
                return true;
            }

            public int getDestinationReg(Word word) {
                return this.getDReg(word);
            }

            public int getRefAddress(Word word, int position, RegisterFile registerFile, Memory memory)
                    throws IllegalMemoryAccessException {
                return position + 1 + this.getPCOffset(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine)
                    throws IllegalMemoryAccessException {
                int var6 = memory.checkAndRead(registerValue + 1 + this.getPCOffset(word)).getValue();
                registerFile.setRegister(this.getDReg(word), var6);
                return registerValue + 1;
            }
        });
        createDef("ST", "1111 ddd ppppppppp", new InstructionDef() {
            public boolean isStore() {
                return true;
            }

            public int getSourceReg1(Word word) {
                return this.getDReg(word);
            }

            public int getRefAddress(Word word, int position, RegisterFile registerFile, Memory memory)
                    throws IllegalMemoryAccessException {
                return position + 1 + this.getPCOffset(word);
            }

            public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine)
                    throws IllegalMemoryAccessException {
                int var6 = registerFile.getRegister(this.getDReg(word));
                memory.checkAndWrite(registerValue + 1 + this.getPCOffset(word), var6);
                return registerValue + 1;
            }
        });
    }

    private class TrapDef extends InstructionDef {

        private TrapDef() {
        }

        // $FF: synthetic method
        TrapDef(Object var2) {
            this();
        }

        public boolean isCall() {
            return true;
        }

        public int execute(Word word, int registerValue, RegisterFile registerFile, Memory memory, Machine machine) {
            registerFile.setPrivMode(true);
            registerFile.setRegister(7, registerValue + 1);
            return word.getZext(8, 0);
        }
    }
}
