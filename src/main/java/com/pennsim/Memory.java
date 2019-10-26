package com.pennsim;

public class Memory extends TableModel {

    static final int MEM_SIZE = 65536;
    private static final int DISABLE_TIMER = 0;
    private static final int MANUAL_TIMER_MODE = 1;
    private static final int BEGIN_DEVICE_REGISTERS = 65024;
    private static final int KBSR = 65024;
    private static final int KBDR = 65026;
    private static final int DSR = 65028;
    private static final int DDR = 65030;
    private static final int TMR = 65032;
    private static final int TMI = 65034;
    private static final int MPR = 65042;
    private static final int MCR = 65534;
    private static final int BREAKPOINT_COLUMN = 0;
    private static final int ADDRESS_COLUMN = 1;
    private static final int VALUE_COLUMN = 2;
    private static final int INSTRUCTION_COLUMN = 3;
    private final Machine machine;
    private Word[] memArr = new Word[MEM_SIZE];
    private String[] colNames = new String[]{"BP", "Address", "Value", "Instruction"};
    private boolean[] nextBreakPoints = new boolean[MEM_SIZE];
    private boolean[] breakPoints = new boolean[MEM_SIZE];
    private KeyboardDevice keyboard = new KeyboardDevice();
    private MonitorDevice monitor = new MonitorDevice();
    private TimerDevice timer = new TimerDevice();

    Memory(Machine machine) {
        this.machine = machine;

        for (int row = 0; row < MEM_SIZE; ++row) {
            this.memArr[row] = new Word();
            this.breakPoints[row] = false;
        }

        this.timer.setTimer();
    }

    /**
     * Get the Keyboard Device being used
     *
     * @return KeyboardDevice object
     */
    KeyboardDevice getKeyBoardDevice() {
        return this.keyboard;
    }

    /**
     * Returns the Monitor Device being used
     *
     * @return MonitorDevice object
     */
    MonitorDevice getMonitor() {
        return this.monitor;
    }

    /**
     * Resets the following parts of the simulator:
     * * Every row in the memory table
     * * Keyboard device
     * * Monitor device
     * * Timer device
     * * Clears all breakpoints
     */
    public void reset() {
        for (int row = 0; row < MEM_SIZE; ++row) {
            this.memArr[row].reset();
        }

        this.keyboard.reset();
        this.monitor.reset();
        this.timer.reset();
        this.clearAllBreakPoints();
        this.fireTableDataChanged();
    }

    /**
     * Returns the amount of rows in the memory table
     *
     * @return number of memory rows
     */
    public int getRowCount() {
        return this.memArr.length;
    }

    /**
     * Returns the amount of columns in the memory table
     *
     * @return number of memory columns
     */
    public int getColumnCount() {
        return this.colNames.length;
    }

    /**
     * Returns the amount of columns in the memory table
     *
     * @return number of memory columns
     */
    public String getColumnName(int column) {
        return this.colNames[column];
    }

    /**
     * Figure out if a cell is editable in the memory table based off
     * of the given row and column indexes
     *
     * @param row the row the cell you are looking for is in
     * @param column the column the cell you are looking for is in
     * @return if the cell is editable
     */
    public boolean isCellEditable(int row, int column) {
        return (column == VALUE_COLUMN || column == BREAKPOINT_COLUMN) && row < BEGIN_DEVICE_REGISTERS;
    }

    /**
     * Check of a breakpoint is set in the given row
     *
     * @param row the row to check for a breakpoint in
     * @return if the breakpoint is set
     */
    boolean isBreakPointSet(int row) {
        return this.breakPoints[row];
    }

    /**
     * Public function to set a breakpoint
     *
     * @param address row to set breakpoint
     * @return either the address where the breakpoint was set or an error
     */
    String setBreakPoint(String address) {
        int row = this.machine.getAddress(address);
        String str;
        if (row != Integer.MAX_VALUE) {
            str = this.setBreakPoint(row);
            if (this.machine.existSym(address)) {
                str = str + " ('" + address + "')";
            }
        } else {
            str = "Error: Invalid address or label ('" + address + "')";
        }

        return str;
    }

    /**
     * PRIVATE function to set a breakpoint
     *
     * @param row row to set breakpoint
     * @return either the index where the breakpoint was set or an error
     */
    private String setBreakPoint(int row) {
        if (row >= 0 && row < MEM_SIZE) {
            this.breakPoints[row] = true;
            this.fireTableCellUpdated(row, -1);
            return "Breakpoint set at " + Word.toHex(row);
        } else {
            return "Error: Invalid address or label";
        }
    }


    /**
     * PRIVATE function to clear a breakpoint
     *
     * @param address row to set breakpoint
     * @return either the index where the breakpoint was set or an error
     */
    String clearBreakPoint(String address) {
        int row = this.machine.getAddress(address);
        String ret;
        if (row != Integer.MAX_VALUE) {
            ret = this.clearBreakPoint(row);
            if (this.machine.existSym(address)) {
                ret = ret + " ('" + address + "')";
            }
        } else {
            ret = "Error: Invalid address or label ('" + address + "')";
        }

        return ret;
    }

    private String clearBreakPoint(int row) {
        if (row >= 0 && row < MEM_SIZE) {
            this.breakPoints[row] = false;
            this.fireTableCellUpdated(row, -1);
            return "Breakpoint cleared at " + Word.toHex(row);
        } else {
            return "Error: Invalid address or label";
        }
    }

    private void clearAllBreakPoints() {
        for (int row = 0; row < MEM_SIZE; ++row) {
            this.breakPoints[row] = false;
            this.nextBreakPoints[row] = false;
        }

    }

    void setNextBreakPoint(int row) {
        assert 0 <= row && row < MEM_SIZE;

        this.nextBreakPoints[row] = true;
    }

    boolean isNextBreakPointSet(int row) {
        assert 0 <= row && row < MEM_SIZE;

        return this.nextBreakPoints[row];
    }

    void clearNextBreakPoint(int row) {
        assert 0 <= row && row < MEM_SIZE;

        this.nextBreakPoints[row] = false;
    }

    public Object getValueAt(int row, int column) {
        Object value = null;
        switch (column) {
            case BREAKPOINT_COLUMN:
                value = this.isBreakPointSet(row);
                break;
            case ADDRESS_COLUMN:
                value = Word.toHex(row);
                String symbol = this.machine.lookupSym(row);
                if (symbol != null) {
                    value += " " + symbol;
                }
                break;
            case VALUE_COLUMN:
                if (row < BEGIN_DEVICE_REGISTERS) {
                    value = this.memArr[row].toHex();
                } else {
                    value = "???";
                }
                break;
            case INSTRUCTION_COLUMN:
                if (row < BEGIN_DEVICE_REGISTERS) {
                    value = ISA.disassemble(this.memArr[row], row, this.machine);
                } else {
                    value = "Use 'list' to query";
                }
        }

        return value;
    }

    Word getInstruction(int row) {
        return this.memArr[row];
    }

    Word checkAndRead(int row) throws IllegalMemoryAccessException {
        this.machine.getRegisterFile().checkAddress(row);
        return this.read(row);
    }

    Word read(int row) {
        Word word;
        switch (row) {
            case KBSR:
                word = this.keyboard.status();
                break;
            case KBDR:
                word = this.keyboard.read();
                break;
            case DSR:
                word = this.monitor.getStatus();
                break;
            case TMR:
                word = this.timer.status();
                break;
            case TMI:
                word = new Word((int) this.timer.getInterval());
                break;
            case MPR:
                word = new Word(this.machine.getRegisterFile().getMPR());
                break;
            case MCR:
                word = new Word(this.machine.getRegisterFile().getMCR());
                break;
            default:
                if (row < 0 || row >= MEM_SIZE) {
                    return null;
                }

                word = this.memArr[row];
        }

        return word;
    }

    /**
     * Set the value of a specific cell
     *
     * @param value the data to be set which is either a String or a Boolean
     * @param row row which cell resides in
     * @param column column which cell resides in
     */
    public void setValueAt(Object value, int row, int column) {
        if (column == VALUE_COLUMN) {
            this.write(row, Word.parseNum((String) value));
            this.fireTableCellUpdated(row, column);
        }

        if (column == BREAKPOINT_COLUMN) {
            if ((Boolean) value) {
                Console.println(this.setBreakPoint(row));
            } else {
                Console.println(this.clearBreakPoint(row));
            }

        }
    }

    void checkAndWrite(int row, int value) throws IllegalMemoryAccessException {
        this.machine.getRegisterFile().checkAddress(row);
        this.write(row, value);
    }

    void write(int row, int value) {
        switch (row) {
            case DDR:
                this.monitor.write((char) value);
                this.fireTableCellUpdated(row, 3);
                break;
            case TMI:
                this.timer.setTimer(value);
                if (value == DISABLE_TIMER) {
                    this.timer.setEnabled(false);
                } else {
                    this.timer.setEnabled(true);
                    if (value == MANUAL_TIMER_MODE) {
                        this.timer.setTimer(this.keyboard);
                    }
                }
                break;
            case MPR:
                this.machine.getRegisterFile().setMPR(value);
                break;
            case MCR:
                this.machine.getRegisterFile().setMCR(value);
                // '耀' == 32768
                if ((value & 32768) == 0) {
                    this.machine.stopExecution(1, true);
                } else {
                    this.machine.updateStatusLabel();
                }
        }

        this.memArr[row].setValue(value);
        this.fireTableCellUpdated(row, INSTRUCTION_COLUMN);
    }
}
