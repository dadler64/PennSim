package com.pennsim;

import com.pennsim.exception.IllegalMemoryAccessException;
import com.pennsim.gui.Console;
import com.pennsim.gui.TableModel;

public class RegisterFile extends TableModel {

    private static final int NUM_REGISTERS = 8;
    private static final int NUM_ROWS = 12;
    private static final int PC_ROW = 8;
    private static final int MPR_ROW = 9;
    private static final int PSR_ROW = 10;
    private static final int CC_ROW = 11;
    private static final String[] indNames = new String[]{"R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "PC", "MPR", "PSR", "CC"};
    private static final int[] indRow = new int[]{0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5};
    private static final int[] indCol = new int[]{1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3};
    private final String[] colNames = new String[]{"Register", "Value", "Register", "Value"};
    private final Machine machine;
    private final Word PC;
    private final Word MPR;
    private final Word PSR;
    private final Word MCR;
    private final Word[] registerArr = new Word[NUM_REGISTERS];
    private boolean dirty;
    private int mostRecentlyWrittenValue;

    RegisterFile(Machine machine) {
        this.machine = machine;
        removeCC();

        for (int register = 0; register < NUM_REGISTERS; ++register) {
            this.registerArr[register] = new Word();
        }

        this.PC = new Word();
        this.MPR = new Word();
        this.MCR = new Word();
        this.PSR = new Word();
        this.reset();
    }

    /**
     * Static method to r emove the CC if LC3 is not being used
     */
    private static void removeCC() {
        if (!PennSim.isLC3()) {
            indNames[CC_ROW] = "";
        }
    }

    public static boolean isLegalRegister(int register) {
        return register >= 0 && register <= NUM_REGISTERS;
    }

    public void reset() {
        for (int register = 0; register < NUM_REGISTERS; ++register) {
            this.registerArr[register].setValue(0);
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

    public String getColumnName(int columnIndex) {
        return this.colNames[columnIndex];
    }

    public boolean isCellEditable(int row, int columnIndex) {
        return columnIndex == 1 || columnIndex == 3;
    }

    public Object getValueAt(int var1, int var2) {
        if (var2 == 0) {
            return indNames[var1];
        } else if (var2 == 1) {
            return this.registerArr[var1].toHex();
        } else if (var2 == 2) {
            return indNames[var1 + 6];
        } else {
            if (var2 == 3) {
                if (var1 < 2) {
                    return this.registerArr[var1 + 6].toHex();
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

    public void setValueAt(Object value, int var2, int var3) {
        if (var3 == 1) {
            this.registerArr[var2].setValue(Word.parseNum((String) value));
        } else if (var3 == 3) {
            if (var2 < 2) {
                this.registerArr[var2 + 6].setValue(Word.parseNum((String) value));
            } else {
                if (var2 == 5) {
                    this.setNZP((String) value);
                    return;
                }

                if (value == null && var2 == 3) {
                    this.fireTableCellUpdated(var2, var3);
                    return;
                }

                int var4 = Word.parseNum((String) value);
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

    boolean isDirty() {
        boolean dirty = this.dirty;
        this.dirty = false;
        return dirty;
    }

    int getMostRecentlyWrittenValue() {
        return this.mostRecentlyWrittenValue;
    }

    public int getPC() {
        return this.PC.getValue();
    }

    public void setPC(int value) {
        int pcValue = this.PC.getValue();
        this.PC.setValue(value);
        this.fireTableCellUpdated(indRow[PC_ROW], indCol[PC_ROW]);
        this.machine.getMemory().fireTableRowsUpdated(pcValue, pcValue);
        this.machine.getMemory().fireTableRowsUpdated(value, value);
    }

    public void incPC(int value) {
        this.setPC(this.PC.getValue() + value);
    }

    public String printRegister(int register) throws IndexOutOfBoundsException {
        if (register >= 0 && register < NUM_REGISTERS) {
            return this.registerArr[register].toHex();
        } else {
            throw new IndexOutOfBoundsException("Register index must be from 0 to 7");
        }
    }

    public int getRegister(int regiister) throws IndexOutOfBoundsException {
        if (regiister >= 0 && regiister < NUM_REGISTERS) {
            return this.registerArr[regiister].getValue();
        } else {
            throw new IndexOutOfBoundsException("Register index must be from 0 to 7");
        }
    }

    public void setRegister(int register, int value) {
        if (register >= 0 && register < NUM_REGISTERS) {
            this.dirty = true;
            this.mostRecentlyWrittenValue = value;
            this.registerArr[register].setValue(value);
            this.fireTableCellUpdated(indRow[register], indCol[register]);
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
        int psrValue = this.PSR.getValue();
        if (!var1) {
            psrValue &= 32767;
        } else {
            psrValue |= 32768;
        }

        this.setPSR(psrValue);
    }

    public void checkAddress(int row) throws IllegalMemoryAccessException {
        boolean isPrivMode = this.getPrivMode();
        if (row >= 0 && row < Memory.MEM_SIZE) {
            if (!isPrivMode) {
                int var3 = row >> NUM_ROWS;
                int var4 = 1 << var3;
                int var5 = this.getMPR();
                if ((var4 & var5) == 0) {
                    throw new IllegalMemoryAccessException(row);
                }
            }
        } else {
            throw new IllegalMemoryAccessException(row);
        }
    }

    public void checkAddress(Word word) throws IllegalMemoryAccessException {
        this.checkAddress(word.getValue());
    }

    public String printCC() {
        if (this.getN() ^ this.getZ() ^ this.getP() && (!this.getN() || !this.getZ() || !this.getP())) {
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

    public void setPSR(int value) {
        this.PSR.setValue(value);
        this.fireTableCellUpdated(indRow[PSR_ROW], indCol[PSR_ROW]);
        this.fireTableCellUpdated(indRow[CC_ROW], indCol[CC_ROW]);
    }

    public void setNZP(int value) {
        int psrValue = this.PSR.getValue();
        psrValue &= -8;
        value &= 65535;
        if ((value & 32768) != 0) {
            psrValue |= 4;
        } else if (value == 0) {
            psrValue |= 2;
        } else {
            psrValue |= 1;
        }

        this.setPSR(psrValue);
    }

    private void setNZP(String inputConditionCode) {
        String conditionCode = inputConditionCode.toLowerCase().trim();
        if (!conditionCode.equals("n") && !conditionCode.equals("z") && !conditionCode.equals("p")) {
            Console.println("Condition codes must be set as one of `n', `z' or `p'");
        } else {
            if (conditionCode.equals("n")) {
                this.setN();
            } else if (conditionCode.equals("z")) {
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

    boolean getClockMCR() {
        return (this.getMCR() & 32768) != 0;
    }

    void setClockMCR(boolean var1) {
        if (var1) {
            this.setMCR(this.MCR.getValue() | 32768);
        } else {
            this.setMCR(this.MCR.getValue() & 32767);
        }

    }

    int getMCR() {
        return this.MCR.getValue();
    }

    void setMCR(int value) {
        this.MCR.setValue(value);
    }

    public int getMPR() {
        return this.MPR.getValue();
    }

    void setMPR(int value) {
        this.MPR.setValue(value);
        this.fireTableCellUpdated(indRow[MPR_ROW], indCol[MPR_ROW]);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[");

        for (int register = 0; register < NUM_REGISTERS; ++register) {
            builder.append("R").append(register).append(": ").append(this.registerArr[register].toHex())
                    .append(register != 7 ? "," : "");
        }

        builder.append("]");
        builder.append("\nPC = ").append(this.PC.toHex());
        builder.append("\nMPR = ").append(this.MPR.toHex());
        builder.append("\nPSR = ").append(this.PSR.toHex());
        builder.append("\nCC = ").append(this.printCC());
        return builder.toString();
    }
}
