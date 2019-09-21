package com.pennsim;

public class P37X extends ISA {

    public void init() {
        super.init();
        createDef("ADD", "0000 ddd sss ttt 100", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg1(Word var1) {
                return this.getSReg(var1);
            }

            public int getSourceReg2(Word var1) {
                return this.getTReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 =
                        var3.getRegister(this.getSReg(var1)) + var3.getRegister(this.getTReg(var1));
                var3.setRegister(this.getDReg(var1), var6);
                return var2 + 1;
            }
        });
        createDef("SUB", "0000 ddd sss ttt 101", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg1(Word var1) {
                return this.getSReg(var1);
            }

            public int getSourceReg2(Word var1) {
                return this.getTReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 =
                        var3.getRegister(this.getSReg(var1)) - var3.getRegister(this.getTReg(var1));
                var3.setRegister(this.getDReg(var1), var6);
                return var2 + 1;
            }
        });
        createDef("MUL", "0000 ddd sss ttt 110", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg1(Word var1) {
                return this.getSReg(var1);
            }

            public int getSourceReg2(Word var1) {
                return this.getTReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 =
                        var3.getRegister(this.getSReg(var1)) * var3.getRegister(this.getTReg(var1));
                var3.setRegister(this.getDReg(var1), var6);
                return var2 + 1;
            }
        });
        createDef("OR", "0001 ddd sss ttt 000", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg1(Word var1) {
                return this.getSReg(var1);
            }

            public int getSourceReg2(Word var1) {
                return this.getTReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 =
                        var3.getRegister(this.getSReg(var1)) | var3.getRegister(this.getTReg(var1));
                var3.setRegister(this.getDReg(var1), var6);
                return var2 + 1;
            }
        });
        createDef("NOT", "0001 ddd sss xxx 001", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg1(Word var1) {
                return this.getSReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = ~var3.getRegister(this.getSReg(var1));
                var3.setRegister(this.getDReg(var1), var6);
                return var2 + 1;
            }
        });
        createDef("AND", "0001 ddd sss ttt 010", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg1(Word var1) {
                return this.getSReg(var1);
            }

            public int getSourceReg2(Word var1) {
                return this.getTReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 =
                        var3.getRegister(this.getSReg(var1)) & var3.getRegister(this.getTReg(var1));
                var3.setRegister(this.getDReg(var1), var6);
                return var2 + 1;
            }
        });
        createDef("XOR", "0001 ddd sss ttt 011", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg1(Word var1) {
                return this.getSReg(var1);
            }

            public int getSourceReg2(Word var1) {
                return this.getTReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 =
                        var3.getRegister(this.getSReg(var1)) ^ var3.getRegister(this.getTReg(var1));
                var3.setRegister(this.getDReg(var1), var6);
                return var2 + 1;
            }
        });
        createDef("SLL", "0001 ddd sss ttt 100", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg1(Word var1) {
                return this.getSReg(var1);
            }

            public int getSourceReg2(Word var1) {
                return this.getTReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getSReg(var1)) << (
                        var3.getRegister(this.getTReg(var1)) & 15);
                var3.setRegister(this.getDReg(var1), var6);
                return var2 + 1;
            }
        });
        createDef("SRL", "0001 ddd sss ttt 101", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg1(Word var1) {
                return this.getSReg(var1);
            }

            public int getSourceReg2(Word var1) {
                return this.getTReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getSReg(var1)) >>> (
                        var3.getRegister(this.getTReg(var1)) & 15);
                var3.setRegister(this.getDReg(var1), var6);
                return var2 + 1;
            }
        });
        createDef("SRA", "0001 ddd sss ttt 110", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg1(Word var1) {
                return this.getSReg(var1);
            }

            public int getSourceReg2(Word var1) {
                return this.getTReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = (short) var3.getRegister(this.getSReg(var1)) >> (
                        var3.getRegister(this.getTReg(var1)) & 15);
                var3.setRegister(this.getDReg(var1), var6);
                return var2 + 1;
            }
        });
        createDef("GETC", "0010 0000 00100000", new P37X.TrapDef());
        createDef("PUTC", "0010 0000 00100001", new P37X.TrapDef());
        createDef("PUTS", "0010 0000 00100010", new P37X.TrapDef());
        createDef("EGETC", "0010 0000 00100011", new P37X.TrapDef());
        createDef("HALT", "0010 0000 00100101", new P37X.TrapDef());
        createDef("TRAP", "0010 0000 uuuuuuuu", new P37X.TrapDef());
        createDef("RTT", "0011 ddd xxxxxxxxx", new InstructionDef() {
            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                var3.setPrivMode(false);
                return var3.getRegister(this.getDReg(var1));
            }
        });
        createDef("JUMP", "0100 pppppppppppp", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                return var2 + 1 + this.getPCOffset(var1);
            }
        });
        createDef("JUMPR", "0101 ddd xxxxxxxxx", new InstructionDef() {
            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                return var3.getRegister(this.getDReg(var1));
            }
        });
        createDef("JSR", "0110 pppppppppppp", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return 7;
            }

            public boolean isCall() {
                return true;
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                var3.setRegister(7, var2 + 1);
                return var2 + 1 + this.getPCOffset(var1);
            }
        });
        createDef("JSRR", "0111 ddd xxxxxxxxx", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return 7;
            }

            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
            }

            public boolean isCall() {
                return true;
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getDReg(var1));
                var3.setRegister(7, var2 + 1);
                return var6;
            }
        });
        createDef("NOOP", "1000 xxx 000 xxxxxx", new InstructionDef() {
            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                return var2 + 1;
            }
        });
        createDef("BRP", "1000 ddd 001 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getDReg(var1)) & '\uffff';
                return var6 != 0 && (var6 & '耀') == 0 ? var2 + 1 + this.getPCOffset(var1)
                        : var2 + 1;
            }
        });
        createDef("BRZ", "1000 ddd 010 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getDReg(var1)) & '\uffff';
                return var6 == 0 ? var2 + 1 + this.getPCOffset(var1) : var2 + 1;
            }
        });
        createDef("BRZP", "1000 ddd 011 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getDReg(var1)) & '\uffff';
                return var6 != 0 && (var6 & '耀') != 0 ? var2 + 1
                        : var2 + 1 + this.getPCOffset(var1);
            }
        });
        createDef("BRN", "1000 ddd 100 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getDReg(var1)) & '\uffff';
                return (var6 & '耀') != 0 ? var2 + 1 + this.getPCOffset(var1) : var2 + 1;
            }
        });
        createDef("BRNP", "1000 ddd 101 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getDReg(var1)) & '\uffff';
                return var6 != 0 ? var2 + 1 + this.getPCOffset(var1) : var2 + 1;
            }
        });
        createDef("BRNZ", "1000 ddd 110 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getDReg(var1)) & '\uffff';
                return var6 != 0 && (var6 & '耀') == 0 ? var2 + 1
                        : var2 + 1 + this.getPCOffset(var1);
            }
        });
        createDef("BRNZP", "1000 ddd 111 pppppp", new InstructionDef() {
            public boolean isBranch() {
                return true;
            }

            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                return var2 + 1 + this.getPCOffset(var1);
            }
        });
        createDef("CONST", "1001 ddd iiiiiiiii", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                var3.setRegister(this.getDReg(var1), this.getSignedImmed(var1));
                return var2 + 1;
            }
        });
        createDef("INC", "1010 ddd iiiiiiiii", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                int var6 = var3.getRegister(this.getDReg(var1)) + this.getSignedImmed(var1);
                var3.setRegister(this.getDReg(var1), var6);
                return var2 + 1;
            }
        });
        createDef("LEA", "1011 ddd ppppppppp", new InstructionDef() {
            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
                var3.setRegister(this.getDReg(var1), var2 + 1 + this.getPCOffset(var1));
                return var2 + 1;
            }
        });
        createDef("LDR", "1100 ddd sss iiiiii", new InstructionDef() {
            public boolean isLoad() {
                return true;
            }

            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg1(Word var1) {
                return this.getSReg(var1);
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
                return var2 + 1;
            }
        });
        createDef("STR", "1101 ddd sss iiiiii", new InstructionDef() {
            public boolean isStore() {
                return true;
            }

            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
            }

            public int getSourceReg2(Word var1) {
                return this.getSReg(var1);
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
        createDef("LD", "1110 ddd ppppppppp", new InstructionDef() {
            public boolean isLoad() {
                return true;
            }

            public int getDestinationReg(Word var1) {
                return this.getDReg(var1);
            }

            public int getRefAddr(Word var1, int var2, RegisterFile var3, Memory var4)
                    throws IllegalMemAccessException {
                return var2 + 1 + this.getPCOffset(var1);
            }

            public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5)
                    throws IllegalMemAccessException {
                int var6 = var4.checkAndRead(var2 + 1 + this.getPCOffset(var1)).getValue();
                var3.setRegister(this.getDReg(var1), var6);
                return var2 + 1;
            }
        });
        createDef("ST", "1111 ddd ppppppppp", new InstructionDef() {
            public boolean isStore() {
                return true;
            }

            public int getSourceReg1(Word var1) {
                return this.getDReg(var1);
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

        public int execute(Word var1, int var2, RegisterFile var3, Memory var4, Machine var5) {
            var3.setPrivMode(true);
            var3.setRegister(7, var2 + 1);
            return var1.getZext(8, 0);
        }
    }
}
