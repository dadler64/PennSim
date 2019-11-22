package com.pennsim;

import com.pennsim.exception.GenericException;
import com.pennsim.exception.IllegalInstructionException;
import com.pennsim.exception.IllegalMemoryAccessException;
import com.pennsim.gui.Console;
import com.pennsim.gui.GUI;
import com.pennsim.util.ErrorLog;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.swing.SwingUtilities;

public class Machine implements Runnable {

    private static final int NUM_CONTINUES = 400;
    private final Hashtable<String, Integer> symbolTable = new Hashtable<>();
    private final Hashtable<Integer, String> inverseTable = new Hashtable<>();
    private final Hashtable<Integer, Boolean> addressToInstructionTable = new Hashtable<>();
    int cycleCount = 0;
    int instructionCount = 0;
    int loadStallCount = 0;
    int branchStallCount = 0;
    private Memory memory;
    private RegisterFile registers;
    private BranchPredictor branchPredictor;
    private GUI gui = null;
    private LinkedList<ActionListener> notifyOnStop;
    private PrintWriter traceWriter = null;
    private boolean stopImmediately = false;
    private boolean continueMode = false;

    public Machine() {
        if (PennSim.isP37X()) {
            (new P37X()).init();
        } else if (PennSim.isLC3()) {
            (new LC3()).init();
        }

        this.memory = new Memory(this);
        this.registers = new RegisterFile(this);
        this.branchPredictor = new BranchPredictor(8);
        this.notifyOnStop = new LinkedList<>();
    }

    public GUI getGUI() {
        return this.gui;
    }

    void setGUI(GUI gui) {
        this.gui = gui;
    }

    public void setStoppedListener(ActionListener listener) {
        this.notifyOnStop.add(listener);
    }

    public void reset() {
        this.symbolTable.clear();
        this.inverseTable.clear();
        this.addressToInstructionTable.clear();
        this.memory.reset();
        this.registers.reset();
        if (this.gui != null) {
            this.gui.reset();
        }

        if (this.isTraceEnabled()) {
            this.disableTrace();
        }

        this.cycleCount = 0;
        this.instructionCount = 0;
        this.loadStallCount = 0;
        this.branchStallCount = 0;
    }

    public void cleanup() {
        ErrorLog.logClose();
        if (this.isTraceEnabled()) {
            this.disableTrace();
        }

    }

    public Memory getMemory() {
        return this.memory;
    }

    public RegisterFile getRegisterFile() {
        return this.registers;
    }

    BranchPredictor getBranchPredictor() {
        return this.branchPredictor;
    }

    PrintWriter getTraceWriter() {
        return this.traceWriter;
    }

    void setTraceWriter(PrintWriter printWriter) {
        this.traceWriter = printWriter;
    }

    boolean isTraceEnabled() {
        return this.traceWriter != null;
    }

    void disableTrace() {
        this.traceWriter.close();
        this.traceWriter = null;
    }

    /**
     * Function to load the symbol file (.sym) into the system
     *
     * @param file the symbol file
     * @return a String confirming that the file was either loaded or was not loaded
     */
    private String loadSymbolTable(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            int count = 0;

            while (reader.ready()) {
                String line = reader.readLine();
                ++count;
                if (count >= 5) {
                    String[] lineArr = line.split("\\s+");
                    if (lineArr.length >= 3) {
                        int value = Word.parseNum("x" + lineArr[2]);
                        if ("$".equals(lineArr[1])) {
                            this.addressToInstructionTable.put(value, true);
                        } else {
                            this.symbolTable.put(lineArr[1].toLowerCase(), value);
                            this.inverseTable.put(value, lineArr[1]);
                        }
                    }
                }
            }

            return "Loaded symbol file '" + file.getPath() + "'";
        } catch (IOException e) {
            return "Could not load symbol file '" + file.getPath() + "'";
        }
    }

    public boolean isContinueMode() {
        return this.continueMode;
    }

    private void setContinueMode() {
        this.continueMode = true;
    }

    private void clearContinueMode() {
        this.continueMode = false;
    }

    public String loadObjectFile(File file) {
        byte[] bytes = new byte[2];
        String inputFilePath = file.getPath();
        if (!inputFilePath.endsWith(".obj")) {
            return "Error: object filename '" + inputFilePath + "' does not end with .obj";
        } else {
            String result;
            try {
                FileInputStream inputStream = new FileInputStream(file);
                inputStream.read(bytes);
                int convertedByteArr = Word.convertByteArray(bytes[0], bytes[1]);

                while (true) {
                    if (inputStream.read(bytes) != 2) {
                        inputStream.close();
                        result = "Loaded object file '" + inputFilePath + "'";
                        break;
                    }

                    if (this.symbolTable.contains(convertedByteArr)) {
                        String var8 = this.inverseTable.get(convertedByteArr);
                        this.symbolTable.remove(var8.toLowerCase());
                        this.inverseTable.remove(convertedByteArr);
                    }

                    this.memory.write(convertedByteArr, Word.convertByteArray(bytes[0], bytes[1]));
                    ++convertedByteArr;
                }
            } catch (IOException var9) {
                return "Error: Could not load object file '" + inputFilePath + "'";
            }

            String filePath = inputFilePath;
            if (inputFilePath.endsWith(".obj")) {
                filePath = inputFilePath.substring(0, inputFilePath.length() - 4);
            }

            filePath = filePath + ".sym";
            result = result + "\n" + this.loadSymbolTable(new File(filePath));
            return result;
        }
    }

    String setKeyboardInputStream(File file) {
        String response;
        try {
            this.memory.getKeyBoardDevice().setInputStream(new FileInputStream(file));
            this.memory.getKeyBoardDevice().setInputMode(KeyboardDevice.SCRIPT_MODE);
            response = "Keyboard input file '" + file.getPath() + "' enabled";
            if (this.gui != null) {
                this.gui.setTextConsoleEnabled(false);
            }
        } catch (FileNotFoundException e) {
            response = "Could not open keyboard input file '" + file.getPath() + "'";
            if (this.gui != null) {
                this.gui.setTextConsoleEnabled(true);
            }
        }

        return response;
    }

    public void executeStep() throws GenericException {
        this.registers.setClockMCR(true);
        this.stopImmediately = false;
        this.executePumpedContinues(1);
        this.updateStatusLabel();
        if (this.gui != null) {
            this.gui.scrollToPC(0);
        }

    }

    public void executeNext() throws GenericException {
        if (ISA.isCall(this.memory.read(this.registers.getPC()))) {
            this.memory.setNextBreakPoint((this.registers.getPC() + 1) % Memory.MEM_SIZE);
            this.executeMany();
        } else {
            this.executeStep();
        }

    }

    public synchronized String stopExecution(boolean var1) {
        return this.stopExecution(0, var1);
    }

    synchronized String stopExecution(int address, boolean var2) {
        this.stopImmediately = true;
        this.clearContinueMode();
        this.updateStatusLabel();
        if (this.gui != null) {
            this.gui.scrollToPC(address);
        }

        this.memory.fireTableDataChanged();
        if (var2) {
            ListIterator iterator = this.notifyOnStop.listIterator(0);

            while (iterator.hasNext()) {
                ActionListener listener = (ActionListener) iterator.next();
                listener.actionPerformed(null);
            }
        }

        return "Stopped at " + Word.toHex(this.registers.getPC());
    }

    private void executePumpedContinues() throws GenericException {
        this.executePumpedContinues(NUM_CONTINUES);
    }

    // TODO: Simplify method to decrease "NPath Complexity". Currently has a complexity of 1420.
    private void executePumpedContinues(int var1) throws GenericException {
        int var2 = var1;
        this.registers.setClockMCR(true);
        if (this.gui != null) {
            this.gui.setStatusLabelRunning();
        }

        while (!this.stopImmediately && var2 > 0) {
            try {
                int pc = this.registers.getPC();
                this.registers.checkAddress(pc);
                Word word = this.memory.getInstruction(pc);
                InstructionDefinition instructionDef = ISA.lookupTable[word.getValue()];
                if (instructionDef == null) {
                    throw new IllegalInstructionException("Undefined instruction:  " + word.toHex());
                }

                int var6 = instructionDef.execute(word, pc, this.registers, this.memory, this);
                this.registers.setPC(var6);
                ++this.cycleCount;
                ++this.instructionCount;
                int var7 = this.branchPredictor.getPredictedPC(pc);
                if (var6 != var7) {
                    this.cycleCount += 2;
                    this.branchStallCount += 2;
                    this.branchPredictor.update(pc, var6);
                }

                if (instructionDef.isLoad()) {
                    Word var8 = this.memory.getInstruction(var6);
                    InstructionDefinition var9 = ISA.lookupTable[var8.getValue()];
                    if (var9 == null) {
                        throw new IllegalInstructionException("Undefined instruction:  " + var8.toHex());
                    }

                    if (!var9.isStore()) {
                        int var10 = instructionDef.getDestinationReg(word);
                        if (var10 >= 0 && (var10 == var9.getSourceReg1(var8) || var10 == var9.getSourceReg2(var8))) {
                            ++this.cycleCount;
                            ++this.loadStallCount;
                        }
                    }
                }

                if (this.isTraceEnabled()) {
                    this.generateTrace(instructionDef, pc, word);
                }

                if (this.memory.isBreakPointSet(this.registers.getPC())) {
                    String var12 = "Hit breakpoint at " + Word.toHex(this.registers.getPC());
                    Console.println(var12);
                    this.stopExecution(true);
                }

                if (this.memory.isNextBreakPointSet(this.registers.getPC())) {
                    this.stopExecution(true);
                    this.memory.clearNextBreakPoint(this.registers.getPC());
                }

                --var2;
            } catch (GenericException var11) {
                this.stopExecution(true);
                throw var11;
            }
        }

        if (this.isContinueMode()) {
            SwingUtilities.invokeLater(this);
        }

    }

    public synchronized void executeMany() throws GenericException {
        this.setContinueMode();
        this.stopImmediately = false;

        try {
            this.executePumpedContinues();
        } catch (GenericException e) {
            this.stopExecution(true);
            throw e;
        }
    }

    void generateTrace(InstructionDefinition instructionDef, int address, Word word) throws IllegalMemoryAccessException {
        if (this.isTraceEnabled()) {
            PrintWriter writer = this.getTraceWriter();
            writer.print(Word.toHex(address, false));
            writer.print(" ");
            writer.print(word.toHex(false));
            writer.print(" ");
            if (this.registers.isDirty()) {
                writer.print(Word.toHex(1, false));
                writer.print(" ");
                writer.print(Word.toHex(this.registers.getMostRecentlyWrittenValue(), false));
            } else {
                writer.print(Word.toHex(0, false));
                writer.print(" ");
                writer.print(Word.toHex(0, false));
            }

            writer.print(" ");
            if (instructionDef.isStore()) {
                writer.print(Word.toHex(1, false));
                writer.print(" ");
                writer.print(Word.toHex(instructionDef.getRefAddress(word, address, this.registers, this.memory), false));
                writer.print(" ");
                writer.print(Word.toHex(this.registers.getRegister(instructionDef.getDReg(word)), false));
            } else {
                writer.print(Word.toHex(0, false));
                writer.print(" ");
                writer.print(Word.toHex(0, false));
                writer.print(" ");
                writer.print(Word.toHex(0, false));
            }

            writer.println(" ");
            writer.flush();
        }

    }

    String lookupSym(int address) {
        return this.inverseTable.get(address);
    }

    int lookupSym(String symbol) {
        Integer var2 = this.symbolTable.get(symbol.toLowerCase());
        return var2 != null ? var2 : Integer.MAX_VALUE;
    }

    boolean lookupAddressToInstruction(int address) {
        return this.addressToInstructionTable.get(address) != null;
    }

    boolean existSym(String symbol) {
        return this.symbolTable.get(symbol.toLowerCase()) != null;
    }

    int getAddress(String address) {
        int row = Word.parseNum(address);
        if (row == Integer.MAX_VALUE) {
            row = this.lookupSym(address);
        }

        return row;
    }

    public void run() {
        try {
            this.executePumpedContinues();
        } catch (GenericException e) {
            if (this.gui != null) {
                e.showMessageDialog(null);
            }

            Console.println(e.getMessage());
        }

    }

    void updateStatusLabel() {
        if (this.gui != null) {
            if (!this.registers.getClockMCR()) {
                this.gui.setStatusLabelHalted();
            } else if (this.isContinueMode()) {
                this.gui.setStatusLabelRunning();
            } else {
                this.gui.setStatusLabelSuspended();
            }
        }

    }
}
