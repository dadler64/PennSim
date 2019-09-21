package com.pennsim;

import javax.swing.table.AbstractTableModel;

public class Memory extends AbstractTableModel {

    public static final int MEM_SIZE = 65536;
    public static final int BEGIN_DEVICE_REGISTERS = 65024;
    public static final int KBSR = 65024;
    public static final int KBDR = 65026;
    public static final int DSR = 65028;
    public static final int DDR = 65030;
    public static final int TMR = 65032;
    public static final int TMI = 65034;
    public static final int DISABLE_TIMER = 0;
    public static final int MANUAL_TIMER_MODE = 1;
    public static final int MPR = 65042;
    public static final int MCR = 65534;
    public static final int BREAKPOINT_COLUMN = 0;
    public static final int ADDRESS_COLUMN = 1;
    public static final int VALUE_COLUMN = 2;
    public static final int INSN_COLUMN = 3;
    private final Machine machine;
    private Word[] memArr = new Word[65536];
    private String[] colNames = new String[]{"BP", "Address", "Value", "com.pennsim.Instruction"};
    private boolean[] nextBreakPoints = new boolean[65536];
    private boolean[] breakPoints = new boolean[65536];
    private KeyboardDevice kbDevice = new KeyboardDevice();
    private MonitorDevice monitorDevice = new MonitorDevice();
    private TimerDevice timerDevice = new TimerDevice();

    public Memory(Machine var1) {
        this.machine = var1;

        for (int var2 = 0; var2 < 65536; ++var2) {
            this.memArr[var2] = new Word();
            this.breakPoints[var2] = false;
        }

        this.timerDevice.setTimer();
    }

    public KeyboardDevice getKeyBoardDevice() {
        return this.kbDevice;
    }

    public MonitorDevice getMonitorDevice() {
        return this.monitorDevice;
    }

    public void reset() {
        for (int var1 = 0; var1 < 65536; ++var1) {
            this.memArr[var1].reset();
        }

        this.kbDevice.reset();
        this.monitorDevice.reset();
        this.timerDevice.reset();
        this.clearAllBreakPoints();
        this.fireTableDataChanged();
    }

    public int getRowCount() {
        return this.memArr.length;
    }

    public int getColumnCount() {
        return this.colNames.length;
    }

    public String getColumnName(int var1) {
        return this.colNames[var1];
    }

    public boolean isCellEditable(int var1, int var2) {
        return (var2 == 2 || var2 == 0) && var1 < 65024;
    }

    public boolean isBreakPointSet(int var1) {
        return this.breakPoints[var1];
    }

    public String setBreakPoint(String var1) {
        int var3 = this.machine.getAddress(var1);
        String var2;
        if (var3 != Integer.MAX_VALUE) {
            var2 = this.setBreakPoint(var3);
            if (this.machine.existSym(var1)) {
                var2 = var2 + " ('" + var1 + "')";
            }
        } else {
            var2 = "Error: Invalid address or label ('" + var1 + "')";
        }

        return var2;
    }

    public String setBreakPoint(int var1) {
        if (var1 >= 0 && var1 < 65536) {
            this.breakPoints[var1] = true;
            this.fireTableCellUpdated(var1, -1);
            return "Breakpoint set at " + Word.toHex(var1);
        } else {
            return "Error: Invalid address or label";
        }
    }

    public String clearBreakPoint(String var1) {
        int var3 = this.machine.getAddress(var1);
        String var2;
        if (var3 != Integer.MAX_VALUE) {
            var2 = this.clearBreakPoint(var3);
            if (this.machine.existSym(var1)) {
                var2 = var2 + " ('" + var1 + "')";
            }
        } else {
            var2 = "Error: Invalid address or label ('" + var1 + "')";
        }

        return var2;
    }

    public String clearBreakPoint(int var1) {
        if (var1 >= 0 && var1 < 65536) {
            this.breakPoints[var1] = false;
            this.fireTableCellUpdated(var1, -1);
            return "Breakpoint cleared at " + Word.toHex(var1);
        } else {
            return "Error: Invalid address or label";
        }
    }

    public void clearAllBreakPoints() {
        for (int var1 = 0; var1 < 65536; ++var1) {
            this.breakPoints[var1] = false;
            this.nextBreakPoints[var1] = false;
        }

    }

    public void setNextBreakPoint(int var1) {
        assert 0 <= var1 && var1 < 65536;

        this.nextBreakPoints[var1] = true;
    }

    public boolean isNextBreakPointSet(int var1) {
        assert 0 <= var1 && var1 < 65536;

        return this.nextBreakPoints[var1];
    }

    public void clearNextBreakPoint(int var1) {
        assert 0 <= var1 && var1 < 65536;

        this.nextBreakPoints[var1] = false;
    }

    public Object getValueAt(int var1, int var2) {
        Object var3 = null;
        switch (var2) {
            case 0:
                var3 = new Boolean(this.isBreakPointSet(var1));
                break;
            case 1:
                var3 = Word.toHex(var1);
                String var4 = this.machine.lookupSym(var1);
                if (var4 != null) {
                    var3 = var3 + " " + var4;
                }
                break;
            case 2:
                if (var1 < 65024) {
                    var3 = this.memArr[var1].toHex();
                } else {
                    var3 = "???";
                }
                break;
            case 3:
                if (var1 < 65024) {
                    var3 = ISA.disassemble(this.memArr[var1], var1, this.machine);
                } else {
                    var3 = "Use 'list' to query";
                }
        }

        return var3;
    }

    public Word getInst(int var1) {
        return this.memArr[var1];
    }

    public Word checkAndRead(int var1) throws IllegalMemAccessException {
        this.machine.getRegisterFile().checkAddr(var1);
        return this.read(var1);
    }

    public Word read(int var1) {
        Word var2 = null;
        switch (var1) {
            case 65024:
                var2 = this.kbDevice.status();
                break;
            case 65026:
                var2 = this.kbDevice.read();
                break;
            case 65028:
                var2 = this.monitorDevice.status();
                break;
            case 65032:
                var2 = this.timerDevice.status();
                break;
            case 65034:
                var2 = new Word((int) this.timerDevice.getInterval());
                break;
            case 65042:
                var2 = new Word(this.machine.getRegisterFile().getMPR());
                break;
            case 65534:
                var2 = new Word(this.machine.getRegisterFile().getMCR());
                break;
            default:
                if (var1 < 0 || var1 >= 65536) {
                    return null;
                }

                var2 = this.memArr[var1];
        }

        return var2;
    }

    public void setValueAt(Object var1, int var2, int var3) {
        if (var3 == 2) {
            this.write(var2, Word.parseNum((String) var1));
            this.fireTableCellUpdated(var2, var3);
        }

        if (var3 == 0) {
            if ((Boolean) var1) {
                Console.println(this.setBreakPoint(var2));
            } else {
                Console.println(this.clearBreakPoint(var2));
            }

        }
    }

    public void checkAndWrite(int var1, int var2) throws IllegalMemAccessException {
        this.machine.getRegisterFile().checkAddr(var1);
        this.write(var1, var2);
    }

    public void write(int var1, int var2) {
        switch (var1) {
            case 65030:
                this.monitorDevice.write((char) var2);
                this.fireTableCellUpdated(var1, 3);
                break;
            case 65034:
                this.timerDevice.setTimer(var2);
                if (var2 == 0) {
                    this.timerDevice.setEnabled(false);
                } else {
                    this.timerDevice.setEnabled(true);
                    if (var2 == 1) {
                        this.timerDevice.setTimer(this.kbDevice);
                    }
                }
                break;
            case 65042:
                this.machine.getRegisterFile().setMPR(var2);
                break;
            case 65534:
                this.machine.getRegisterFile().setMCR(var2);
                if ((var2 & '耀') == 0) {
                    this.machine.stopExecution(1, true);
                } else {
                    this.machine.updateStatusLabel();
                }
        }

        this.memArr[var1].setValue(var2);
        this.fireTableCellUpdated(var1, 3);
    }
}
