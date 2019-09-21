package com.pennsim;

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
   private Memory memory;
   private RegisterFile registers;
   private BranchPredictor bpred;
   private GUI gui = null;
   private LinkedList NotifyOnStop;
   private PrintWriter traceWriter = null;
   private final Hashtable symbolTable = new Hashtable();
   private final Hashtable inverseTable = new Hashtable();
   private final Hashtable addrToInsnTable = new Hashtable();
   public int CYCLE_COUNT = 0;
   public int INSTRUCTION_COUNT = 0;
   public int LOAD_STALL_COUNT = 0;
   public int BRANCH_STALL_COUNT = 0;
   public static final int NUM_CONTINUES = 400;
   boolean stopImmediately = false;
   private boolean continueMode = false;

   public Machine() {
      if (PennSim.isP37X()) {
         (new P37X()).init();
      } else if (PennSim.isLC3()) {
         (new LC3()).init();
      }

      this.memory = new Memory(this);
      this.registers = new RegisterFile(this);
      this.bpred = new BranchPredictor(this, 8);
      this.NotifyOnStop = new LinkedList();
   }

   public void setGUI(GUI var1) {
      this.gui = var1;
   }

   public GUI getGUI() {
      return this.gui;
   }

   public void setStoppedListener(ActionListener var1) {
      this.NotifyOnStop.add(var1);
   }

   public void reset() {
      this.symbolTable.clear();
      this.inverseTable.clear();
      this.addrToInsnTable.clear();
      this.memory.reset();
      this.registers.reset();
      if (this.gui != null) {
         this.gui.reset();
      }

      if (this.isTraceEnabled()) {
         this.disableTrace();
      }

      this.CYCLE_COUNT = 0;
      this.INSTRUCTION_COUNT = 0;
      this.LOAD_STALL_COUNT = 0;
      this.BRANCH_STALL_COUNT = 0;
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

   public BranchPredictor getBranchPredictor() {
      return this.bpred;
   }

   public void setTraceWriter(PrintWriter var1) {
      this.traceWriter = var1;
   }

   public PrintWriter getTraceWriter() {
      return this.traceWriter;
   }

   public boolean isTraceEnabled() {
      return this.traceWriter != null;
   }

   public void disableTrace() {
      this.traceWriter.close();
      this.traceWriter = null;
   }

   public String loadSymbolTable(File var1) {
      try {
         BufferedReader var3 = new BufferedReader(new FileReader(var1));
         int var4 = 0;

         while(var3.ready()) {
            String var5 = var3.readLine();
            ++var4;
            if (var4 >= 5) {
               String[] var6 = var5.split("\\s+");
               if (var6.length >= 3) {
                  int var7 = Word.parseNum("x" + var6[2]);
                  if ("$".equals(var6[1])) {
                     this.addrToInsnTable.put(var7, true);
                  } else {
                     this.symbolTable.put(var6[1].toLowerCase(), var7);
                     this.inverseTable.put(var7, var6[1]);
                  }
               }
            }
         }

         String var2 = "Loaded symbol file '" + var1.getPath() + "'";
         return var2;
      } catch (IOException var8) {
         return "Could not load symbol file '" + var1.getPath() + "'";
      }
   }

   public boolean isContinueMode() {
      return this.continueMode;
   }

   public void setContinueMode() {
      this.continueMode = true;
   }

   public void clearContinueMode() {
      this.continueMode = false;
   }

   public String loadObjectFile(File var1) {
      byte[] var2 = new byte[2];
      String var4 = var1.getPath();
      if (!var4.endsWith(".obj")) {
         return "Error: object filename '" + var4 + "' does not end with .obj";
      } else {
         String var3;
         try {
            FileInputStream var5 = new FileInputStream(var1);
            var5.read(var2);
            int var6 = Word.convertByteArray(var2[0], var2[1]);

            while(true) {
               if (var5.read(var2) != 2) {
                  var5.close();
                  var3 = "Loaded object file '" + var4 + "'";
                  break;
               }

               Integer var7 = new Integer(var6);
               if (this.symbolTable.contains(var7)) {
                  String var8 = (String)this.inverseTable.get(var7);
                  this.symbolTable.remove(var8.toLowerCase());
                  this.inverseTable.remove(var7);
               }

               this.memory.write(var6, Word.convertByteArray(var2[0], var2[1]));
               ++var6;
            }
         } catch (IOException var9) {
            return "Error: Could not load object file '" + var4 + "'";
         }

         String var10 = var4;
         if (var4.endsWith(".obj")) {
            var10 = var4.substring(0, var4.length() - 4);
         }

         var10 = var10 + ".sym";
         var3 = var3 + "\n" + this.loadSymbolTable(new File(var10));
         return var3;
      }
   }

   public String setKeyboardInputStream(File var1) {
      String var2;
      try {
         this.memory.getKeyBoardDevice().setInputStream(new FileInputStream(var1));
         this.memory.getKeyBoardDevice().setInputMode(KeyboardDevice.SCRIPT_MODE);
         var2 = "Keyboard input file '" + var1.getPath() + "' enabled";
         if (this.gui != null) {
            this.gui.setTextConsoleEnabled(false);
         }
      } catch (FileNotFoundException var4) {
         var2 = "Could not open keyboard input file '" + var1.getPath() + "'";
         if (this.gui != null) {
            this.gui.setTextConsoleEnabled(true);
         }
      }

      return var2;
   }

   public void executeStep() throws ExceptionException {
      this.registers.setClockMCR(true);
      this.stopImmediately = false;
      this.executePumpedContinues(1);
      this.updateStatusLabel();
      if (this.gui != null) {
         this.gui.scrollToPC(0);
      }

   }

   public void executeNext() throws ExceptionException {
      if (ISA.isCall(this.memory.read(this.registers.getPC()))) {
         this.memory.setNextBreakPoint((this.registers.getPC() + 1) % 65536);
         this.executeMany();
      } else {
         this.executeStep();
      }

   }

   public synchronized String stopExecution(boolean var1) {
      return this.stopExecution(0, var1);
   }

   public synchronized String stopExecution(int var1, boolean var2) {
      this.stopImmediately = true;
      this.clearContinueMode();
      this.updateStatusLabel();
      if (this.gui != null) {
         this.gui.scrollToPC(var1);
      }

      this.memory.fireTableDataChanged();
      if (var2) {
         ListIterator var3 = this.NotifyOnStop.listIterator(0);

         while(var3.hasNext()) {
            ActionListener var4 = (ActionListener)var3.next();
            var4.actionPerformed(null);
         }
      }

      return "Stopped at " + Word.toHex(this.registers.getPC());
   }

   public void executePumpedContinues() throws ExceptionException {
      this.executePumpedContinues(400);
   }

   public void executePumpedContinues(int var1) throws ExceptionException {
      int var2 = var1;
      this.registers.setClockMCR(true);
      if (this.gui != null) {
         this.gui.setStatusLabelRunning();
      }

      while(!this.stopImmediately && var2 > 0) {
         try {
            int var3 = this.registers.getPC();
            this.registers.checkAddr(var3);
            Word var4 = this.memory.getInst(var3);
            InstructionDef var5 = ISA.lookupTable[var4.getValue()];
            if (var5 == null) {
               throw new IllegalInstructionException("Undefined instruction:  " + var4.toHex());
            }

            int var6 = var5.execute(var4, var3, this.registers, this.memory, this);
            this.registers.setPC(var6);
            ++this.CYCLE_COUNT;
            ++this.INSTRUCTION_COUNT;
            int var7 = this.bpred.getPredictedPC(var3);
            if (var6 != var7) {
               this.CYCLE_COUNT += 2;
               this.BRANCH_STALL_COUNT += 2;
               this.bpred.update(var3, var6);
            }

            if (var5.isLoad()) {
               Word var8 = this.memory.getInst(var6);
               InstructionDef var9 = ISA.lookupTable[var8.getValue()];
               if (var9 == null) {
                  throw new IllegalInstructionException("Undefined instruction:  " + var8.toHex());
               }

               if (!var9.isStore()) {
                  int var10 = var5.getDestinationReg(var4);
                  if (var10 >= 0 && (var10 == var9.getSourceReg1(var8) || var10 == var9.getSourceReg2(var8))) {
                     ++this.CYCLE_COUNT;
                     ++this.LOAD_STALL_COUNT;
                  }
               }
            }

            if (this.isTraceEnabled()) {
               this.generateTrace(var5, var3, var4);
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
         } catch (ExceptionException var11) {
            this.stopExecution(true);
            throw var11;
         }
      }

      if (this.isContinueMode()) {
         SwingUtilities.invokeLater(this);
      }

   }

   public synchronized void executeMany() throws ExceptionException {
      this.setContinueMode();
      this.stopImmediately = false;

      try {
         this.executePumpedContinues();
      } catch (ExceptionException var2) {
         this.stopExecution(true);
         throw var2;
      }
   }

   public void generateTrace(InstructionDef var1, int var2, Word var3) throws IllegalMemAccessException {
      if (this.isTraceEnabled()) {
         PrintWriter var4 = this.getTraceWriter();
         var4.print(Word.toHex(var2, false));
         var4.print(" ");
         var4.print(var3.toHex(false));
         var4.print(" ");
         if (this.registers.isDirty()) {
            var4.print(Word.toHex(1, false));
            var4.print(" ");
            var4.print(Word.toHex(this.registers.getMostRecentlyWrittenValue(), false));
         } else {
            var4.print(Word.toHex(0, false));
            var4.print(" ");
            var4.print(Word.toHex(0, false));
         }

         var4.print(" ");
         if (var1.isStore()) {
            var4.print(Word.toHex(1, false));
            var4.print(" ");
            var4.print(Word.toHex(var1.getRefAddr(var3, var2, this.registers, this.memory), false));
            var4.print(" ");
            var4.print(Word.toHex(this.registers.getRegister(var1.getDReg(var3)), false));
         } else {
            var4.print(Word.toHex(0, false));
            var4.print(" ");
            var4.print(Word.toHex(0, false));
            var4.print(" ");
            var4.print(Word.toHex(0, false));
         }

         var4.println(" ");
         var4.flush();
      }

   }

   public String lookupSym(int var1) {
      return (String)this.inverseTable.get(new Integer(var1));
   }

   public int lookupSym(String var1) {
      Object var2 = this.symbolTable.get(var1.toLowerCase());
      return var2 != null ? (Integer)var2 : Integer.MAX_VALUE;
   }

   public boolean lookupAddrToInsn(int var1) {
      return this.addrToInsnTable.get(var1) != null;
   }

   public boolean existSym(String var1) {
      return this.symbolTable.get(var1.toLowerCase()) != null;
   }

   public int getAddress(String var1) {
      int var2 = Word.parseNum(var1);
      if (var2 == Integer.MAX_VALUE) {
         var2 = this.lookupSym(var1);
      }

      return var2;
   }

   public void run() {
      try {
         this.executePumpedContinues();
      } catch (ExceptionException var2) {
         if (this.gui != null) {
            var2.showMessageDialog(null);
         }

         Console.println(var2.getMessage());
      }

   }

   public void updateStatusLabel() {
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
