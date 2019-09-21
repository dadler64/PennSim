package com.pennsim;

public class LC3 extends ISA {

    public void init() {
        super.init();
        createDef("ADD", "0001 ddd sss 0 00 ttt", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 =
                        var3.getRegister(this.getSReg(var1)) + var3.getRegister(this.getTReg(var1));
                var3.setRegister(this.getDReg(var1), var6);
                var3.setNZP(var6);
                return var2 + 1;
            }
        });
        createDef("SUB", "0001 ddd sss 0 10 ttt", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 =
                        var3.getRegister(this.getSReg(var1)) - var3.getRegister(this.getTReg(var1));
                var3.setRegister(this.getDReg(var1), var6);
                var3.setNZP(var6);
                return var2 + 1;
            }
        });
        createDef("ADD", "0001 ddd sss 1 iiiii", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getSReg(var1)) + this.getSignedImmed(var1);
                var3.setRegister(this.getDReg(var1), var6);
                var3.setNZP(var6);
                return var2 + 1;
            }
        });
        createDef("AND", "0101 ddd sss 0 00 ttt", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 =
                        var3.getRegister(this.getSReg(var1)) & var3.getRegister(this.getTReg(var1));
                var3.setRegister(this.getDReg(var1), var6);
                var3.setNZP(var6);
                return var2 + 1;
            }
        });
        createDef("AND", "0101 ddd sss 1 iiiii", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getSReg(var1)) & this.getSignedImmed(var1);
                var3.setRegister(this.getDReg(var1), var6);
                var3.setNZP(var6);
                return var2 + 1;
            }
        });
        createDef("BR", "0000 111 ppppppppp", new LC3.BranchDef());
        createDef("BRnzp", "0000 111 ppppppppp", new LC3.BranchDef());
        createDef("BRp", "0000 001 ppppppppp", new LC3.BranchDef());
        createDef("BRz", "0000 010 ppppppppp", new LC3.BranchDef());
        createDef("BRzp", "0000 011 ppppppppp", new LC3.BranchDef());
        createDef("BRn", "0000 100 ppppppppp", new LC3.BranchDef());
        createDef("BRnp", "0000 101 ppppppppp", new LC3.BranchDef());
        createDef("BRnz", "0000 110 ppppppppp", new LC3.BranchDef());
        createDef("RET", "1100 000 111 000000", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                return var3.getRegister(7);
            }
        });
        createDef("JMP", "1100 000 ddd 000000", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                return var3.getRegister(this.getDReg(var1));
            }
        });
        createDef("RTT", "1100 000 111 000001", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                var3.setPrivMode(false);
                return var3.getRegister(7);
            }
        });
        createDef("JMPT", "1100 000 ddd 000001", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                var3.setPrivMode(false);
                return var3.getRegister(this.getDReg(var1));
            }
        });
        createDef("JSR", "0100 1 ppppppppppp", new InstructionDef() {
            public boolean isCall() {
                return true;
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                var3.setRegister(7, var2 + 1);
                return var2 + 1 + this.getPCOffset(var1);
            }
        });
        createDef("JSRR", "0100 000 ddd 000000", new InstructionDef() {
            public boolean isCall() {
                return true;
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getDReg(var1));
                var3.setRegister(7, var2 + 1);
                return var6;
            }
        });
        createDef("LD", "0010 ddd ppppppppp", new InstructionDef() {
            public boolean isLoad() {
                return true;
            }

            public int getRefAddr(Word var1, int var2, RegisterFile var3, Memory var4)
                    throws IllegalMemAccessException {
                return var2 + 1 + this.getPCOffset(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5)
                    throws IllegalMemAccessException {
                int var6 = var4.checkAndRead(var2 + 1 + this.getPCOffset(var1)).getValue();
                var3.setRegister(this.getDReg(var1), var6);
                var3.setNZP(var6);
                return var2 + 1;
            }
        });
        createDef("LDI", "1010 ddd ppppppppp", new InstructionDef() {
            public boolean isLoad() {
                return true;
            }

            public int getRefAddr(Word var1, int var2, RegisterFile var3, Memory var4)
                    throws IllegalMemAccessException {
                return var4.checkAndRead(var2 + 1 + this.getPCOffset(var1)).getValue();
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5)
                    throws IllegalMemAccessException {
                int var6 = var4.checkAndRead(var2 + 1 + this.getPCOffset(var1)).getValue();
                int var7 = var4.checkAndRead(var6).getValue();
                var3.setRegister(this.getDReg(var1), var7);
                var3.setNZP(var7);
                return var2 + 1;
            }
        });
        createDef("LDR", "0110 ddd sss iiiiii", new InstructionDef() {
            public boolean isLoad() {
                return true;
            }

            public int getRefAddr(Word var1, int var2, RegisterFile var3, Memory var4)
                    throws IllegalMemAccessException {
                return var3.getRegister(this.getSReg(var1)) + this.getSignedImmed(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5)
                    throws IllegalMemAccessException {
                int var6 = var4.checkAndRead(
                        var3.getRegister(this.getSReg(var1)) + this.getSignedImmed(var1))
                        .getValue();
                var3.setRegister(this.getDReg(var1), var6);
                var3.setNZP(var6);
                return var2 + 1;
            }
        });
        createDef("LEA", "1110 ddd ppppppppp", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                var3.setRegister(this.getDReg(var1), var2 + 1 + this.getPCOffset(var1));
                var3.setNZP(var2 + 1 + this.getPCOffset(var1));
                return var2 + 1;
            }
        });
        createDef("ST", "0011 ddd ppppppppp", new InstructionDef() {
            public boolean isStore() {
                return true;
            }

            public int getRefAddr(Word var1, int var2, RegisterFile var3, Memory var4)
                    throws IllegalMemAccessException {
                return var2 + 1 + this.getPCOffset(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5)
                    throws IllegalMemAccessException {
                int var6 = var3.getRegister(this.getDReg(var1));
                var4.checkAndWrite(var2 + 1 + this.getPCOffset(var1), var6);
                return var2 + 1;
            }
        });
        createDef("STI", "1011 ddd ppppppppp", new InstructionDef() {
            public boolean isStore() {
                return true;
            }

            public int getRefAddr(Word var1, int var2, RegisterFile var3, Memory var4)
                    throws IllegalMemAccessException {
                return var4.checkAndRead(var2 + 1 + this.getPCOffset(var1)).getValue();
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5)
                    throws IllegalMemAccessException {
                int var6 = var4.checkAndRead(var2 + 1 + this.getPCOffset(var1)).getValue();
                int var7 = var3.getRegister(this.getDReg(var1));
                var4.checkAndWrite(var6, var7);
                return var2 + 1;
            }
        });
        createDef("STR", "0111 ddd sss iiiiii", new InstructionDef() {
            public boolean isStore() {
                return true;
            }

            public int getRefAddr(Word var1, int var2, RegisterFile var3, Memory var4)
                    throws IllegalMemAccessException {
                return var3.getRegister(this.getSReg(var1)) + this.getSignedImmed(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5)
                    throws IllegalMemAccessException {
                int var6 = var3.getRegister(this.getDReg(var1));
                var4.checkAndWrite(var3.getRegister(this.getSReg(var1)) + this.getSignedImmed(var1),
                        var6);
                return var2 + 1;
            }
        });
        createDef("NOT", "1001 ddd sss 111111", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = ~var3.getRegister(this.getSReg(var1));
                var3.setRegister(this.getDReg(var1), var6);
                var3.setNZP(var6);
                return var2 + 1;
            }
        });
        createDef("MUL", "1101 ddd sss 0 00 ttt", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 =
                        var3.getRegister(this.getSReg(var1)) * var3.getRegister(this.getTReg(var1));
                var3.setRegister(this.getDReg(var1), var6);
                var3.setNZP(var6);
                return var2 + 1;
            }
        });
        createDef("MUL", "1101 ddd sss 1 iiiii", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getSReg(var1)) * this.getSignedImmed(var1);
                var3.setRegister(this.getDReg(var1), var6);
                var3.setNZP(var6);
                return var2 + 1;
            }
        });
        createDef("RTI", "1000 000000000000", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5)
                    throws IllegalInstructionException {
                if (var3.getPrivMode()) {
                    int var6 = var3.getRegister(6);
                    int var7 = var4.read(var6).getValue();
                    var3.setRegister(6, var6 + 1);
                    int var8 = var4.read(var6).getValue();
                    var3.setPSR(var8);
                    return var7;
                } else {
                    throw new IllegalInstructionException(
                            "RTI can only be executed in privileged mode");
                }
            }
        });
        createDef("GETC", "1111 0000 00100000", new LC3.TrapDef());
        createDef("OUT", "1111 0000 00100001", new LC3.TrapDef());
        createDef("PUTS", "1111 0000 00100010", new LC3.TrapDef());
        createDef("IN", "1111 0000 00100011", new LC3.TrapDef());
        createDef("PUTSP", "1111 0000 00100100", new LC3.TrapDef());
        createDef("HALT", "1111 0000 00100101", new LC3.TrapDef());
        createDef("TRAP", "1111 0000 uuuuuuuu", new LC3.TrapDef());
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

        public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5)
                throws IllegalMemAccessException, IllegalInstructionException {
            var3.setPrivMode(true);
            var3.setRegister(7, var2 + 1);
            return var4.read(var1.getZext(8, 0)).getValue();
        }
    }

    private class BranchDef extends InstructionDef {

        private BranchDef() {
        }

        // $FF: synthetic method
        BranchDef(Object var2) {
            this();
        }

        public boolean isBranch() {
            return true;
        }

        public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5)
                throws IllegalMemAccessException, IllegalInstructionException {
            return (var1.getBit(11) != 1 || !var3.getN()) && (var1.getBit(10) != 1 || !var3.getZ())
                    && (var1.getBit(9) != 1 || !var3.getP()) ? var2 + 1
                    : var2 + 1 + this.getPCOffset(var1);
        }
    }
}
