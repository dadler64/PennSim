package com.pennsim;

import javax.swing.table.AbstractTableModel;

public class RegisterFile extends AbstractTableModel {

    public static final int NUM_REGISTERS = 8;
    private static final int NUM_ROWS = 12;
    private static final int PC_ROW = 8;
    private static final int MPR_ROW = 9;
    private static final int PSR_ROW = 10;
    private static final int CC_ROW = 11;
    private static String[] indNames = new String[]{"R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7",
            "PC", "MPR", "PSR", "CC"};
    private static int[] indRow = new int[]{0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5};
    private static int[] indCol = new int[]{1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3};
    private final String[] colNames = new String[]{"Register", "Value", "Register", "Value"};
    private final Machine machine;
    private final Word PC;
    private final Word MPR;
    private final Word PSR;
    private final Word MCR;
    private final Word[] regArr = new Word[8];
    private boolean dirty;
    private int mostRecentlyWrittenValue;

    public RegisterFile(Machine var1) {
        this.machine = var1;
        if (!PennSim.isLC3()) {
            indNames[11] = "";
        }

        for (int var2 = 0; var2 < 8; ++var2) {
            this.regArr[var2] = new Word();
        }

        this.PC = new Word();
        this.MPR = new Word();
        this.MCR = new Word();
        this.PSR = new Word();
        this.reset();
    }

    public static boolean isLegalRegister(int var0) {
        return var0 >= 0 && var0 <= 8;
    }

    public void reset() {
        for (int var1 = 0; var1 < 8; ++var1) {
            this.regArr[var1].setValue(0);
        }

        this.PC.setValue(512);
        this.MPR.setValue(0);
        this.MCR.setValue(32768);
        this.PSR.setValue(2);
        this.setPrivMode(true);
        this.fireTableDataChanged();
    }

    public int getRowCount() {
        return 6;
    }

    public int getColumnCount() {
        return this.colNames.length;
    }

    public String getColumnName(int var1) {
        return this.colNames[var1];
    }

    public boolean isCellEditable(int var1, int var2) {
        return var2 == 1 || var2 == 3;
    }

    public Object getValueAt(int var1, int var2) {
        if (var2 == 0) {
            return indNames[var1];
        } else if (var2 == 1) {
            return this.regArr[var1].toHex();
        } else if (var2 == 2) {
            return indNames[var1 + 6];
        } else {
            if (var2 == 3) {
                if (var1 < 2) {
                    return this.regArr[var1 + 6].toHex();
                }

                if (var1 == 2) {
                    return this.PC.toHex();
                }

                if (var1 == 3) {
                    return this.MPR.toHex();
                }

                if (var1 == 4) {
                    return this.PSR.toHex();
                }

                if (var1 == 5) {
                    if (PennSim.isLC3()) {
                        return this.printCC();
                    }

                    return "";
                }
            }

            return null;
        }
    }

    public void setValueAt(Object var1, int var2, int var3) {
        if (var3 == 1) {
            this.regArr[var2].setValue(Word.parseNum((String) var1));
        } else if (var3 == 3) {
            if (var2 < 2) {
                this.regArr[var2 + 6].setValue(Word.parseNum((String) var1));
            } else {
                if (var2 == 5) {
                    this.setNZP((String) var1);
                    return;
                }

                if (var1 == null && var2 == 3) {
                    this.fireTableCellUpdated(var2, var3);
                    return;
                }

                int var4 = Word.parseNum((String) var1);
                if (var2 == 2) {
                    this.setPC(var4);
                    if (this.machine.getGUI() != null) {
                        this.machine.getGUI().scrollToPC();
                    }
                } else if (var2 == 3) {
                    this.setMPR(var4);
                } else if (var2 == 4) {
                    this.setPSR(var4);
                }
            }
        }

        this.fireTableCellUpdated(var2, var3);
    }

    public boolean isDirty() {
        boolean var1 = this.dirty;
        this.dirty = false;
        return var1;
    }

    public int getMostRecentlyWrittenValue() {
        return this.mostRecentlyWrittenValue;
    }

    public int getPC() {
        return this.PC.getValue();
    }

    public void setPC(int var1) {
        int var2 = this.PC.getValue();
        this.PC.setValue(var1);
        this.fireTableCellUpdated(indRow[8], indCol[8]);
        this.machine.getMemory().fireTableRowsUpdated(var2, var2);
        this.machine.getMemory().fireTableRowsUpdated(var1, var1);
    }

    public void incPC(int var1) {
        this.setPC(this.PC.getValue() + var1);
    }

    public String printRegister(int var1) throws IndexOutOfBoundsException {
        if (var1 >= 0 && var1 < 8) {
            return this.regArr[var1].toHex();
        } else {
            throw new IndexOutOfBoundsException("Register index must be from 0 to 7");
        }
    }

    public int getRegister(int var1) throws IndexOutOfBoundsException {
        if (var1 >= 0 && var1 < 8) {
            return this.regArr[var1].getValue();
        } else {
            throw new IndexOutOfBoundsException("Register index must be from 0 to 7");
        }
    }

    public void setRegister(int var1, int var2) {
        if (var1 >= 0 && var1 < 8) {
            this.dirty = true;
            this.mostRecentlyWrittenValue = var2;
            this.regArr[var1].setValue(var2);
            this.fireTableCellUpdated(indRow[var1], indCol[var1]);
        } else {
            throw new IndexOutOfBoundsException("Register index must be from 0 to 7");
        }
    }

    public boolean getN() {
        return this.PSR.getBit(2) == 1;
    }

    public boolean getZ() {
        return this.PSR.getBit(1) == 1;
    }

    public boolean getP() {
        return this.PSR.getBit(0) == 1;
    }

    public boolean getPrivMode() {
        return this.PSR.getBit(15) == 1;
    }

    public void setPrivMode(boolean var1) {
        int var2 = this.PSR.getValue();
        if (!var1) {
            var2 &= 32767;
        } else {
            var2 |= 32768;
        }

        this.setPSR(var2);
    }

    public void checkAddr(int var1) throws IllegalMemAccessException {
        boolean var2 = this.getPrivMode();
        if (var1 >= 0 && var1 < 65536) {
            if (!var2) {
                int var3 = var1 >> 12;
                int var4 = 1 << var3;
                int var5 = this.getMPR();
                if ((var4 & var5) == 0) {
                    throw new IllegalMemAccessException(var1);
                }
            }
        } else {
            throw new IllegalMemAccessException(var1);
        }
    }

    public void checkAddr(Word var1) throws IllegalMemAccessException {
        this.checkAddr(var1.getValue());
    }

    public String printCC() {
        if (this.getN() ^ this.getZ() ^ this.getP() && (!this.getN() || !this.getZ() || !this
                .getP())) {
            if (this.getN()) {
                return "N";
            } else if (this.getZ()) {
                return "Z";
            } else {
                return this.getP() ? "P" : "unset";
            }
        } else {
            return "invalid";
        }
    }

    public int getPSR() {
        return this.PSR.getValue();
    }

    public void setPSR(int var1) {
        this.PSR.setValue(var1);
        this.fireTableCellUpdated(indRow[10], indCol[10]);
        this.fireTableCellUpdated(indRow[11], indCol[11]);
    }

    public void setNZP(int var1) {
        int var2 = this.PSR.getValue();
        var2 &= -8;
        var1 &= 65535;
        if ((var1 & '耀') != 0) {
            var2 |= 4;
        } else if (var1 == 0) {
            var2 |= 2;
        } else {
            var2 |= 1;
        }

        this.setPSR(var2);
    }

    public void setNZP(String var1) {
        var1 = var1.toLowerCase().trim();
        if (!var1.equals("n") && !var1.equals("z") && !var1.equals("p")) {
            Console.println("Condition codes must be set as one of `n', `z' or `p'");
        } else {
            if (var1.equals("n")) {
                this.setN();
            } else if (var1.equals("z")) {
                this.setZ();
            } else {
                this.setP();
            }

        }
    }

    public void setN() {
        this.setNZP(32768);
    }

    public void setZ() {
        this.setNZP(0);
    }

    public void setP() {
        this.setNZP(1);
    }

    public boolean getClockMCR() {
        return (this.getMCR() & '耀') != 0;
    }

    public void setClockMCR(boolean var1) {
        if (var1) {
            this.setMCR(this.MCR.getValue() | '耀');
        } else {
            this.setMCR(this.MCR.getValue() & 32767);
        }

    }

    public int getMCR() {
        return this.MCR.getValue();
    }

    public void setMCR(int var1) {
        this.MCR.setValue(var1);
    }

    public int getMPR() {
        return this.MPR.getValue();
    }

    public void setMPR(int var1) {
        this.MPR.setValue(var1);
        this.fireTableCellUpdated(indRow[9], indCol[9]);
    }

    public String toString() {
        String var1 = "[";

        for (int var2 = 0; var2 < 8; ++var2) {
            var1 = var1 + "R" + var2 + ": " + this.regArr[var2].toHex() + (var2 != 7 ? "," : "");
        }

        var1 = var1 + "]";
        var1 = var1 + "\nPC = " + this.PC.toHex();
        var1 = var1 + "\nMPR = " + this.MPR.toHex();
        var1 = var1 + "\nPSR = " + this.PSR.toHex();
        var1 = var1 + "\nCC = " + this.printCC();
        return var1;
    }
}
