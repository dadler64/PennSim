package com.pennsim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;
import java.util.TreeSet;

public class CommandLine {

   public static final String NEWLINE = System.getProperty("line.separator");
   public static final String PROMPT;

   static {
      PROMPT = NEWLINE + ">> ";
   }

   private Machine mac;
   private GUI GUI;
   private LinkedList<String> commandQueue;
   private Stack<String> prevHistoryStack;
   private Stack<String> nextHistoryStack;
   private Hashtable<String, Command> commands;
   private TreeSet<Command> commandsSet;
   private int checksPassed = 0;
   private int checksFailed = 0;
   private int checksPassedCumulative = 0;
   private int checksFailedCumulative = 0;

   public CommandLine(Machine var1) {
      this.mac = var1;
      this.commands = new Hashtable<String, Command>();
      this.setupCommands();
      this.commandsSet = new TreeSet<Command>(new Comparator() {
         public int compare(Object var1, Object var2) {
            CommandLine.Command var3 = (CommandLine.Command) var1;
            CommandLine.Command var4 = (CommandLine.Command) var2;
            String var5 = var3.getUsage().split("\\s+")[0];
            String var6 = var4.getUsage().split("\\s+")[0];
            return var5.compareTo(var6);
         }

         // TODO: Resolve unchecked assignment error
//         public boolean equals(Object var1) {
//            com.pennsim.CommandLine.Command var2 = (com.pennsim.CommandLine.Command)var1;
//            String var3 = var2.getUsage().split("\\s+")[0];
//            String var4 = ((com.pennsim.CommandLine.Command)this).getUsage().split("\\s+")[0];
//            return var3.equals(var4);
//         }
      });
      this.commandsSet.addAll(this.commands.values());
      this.commandQueue = new LinkedList<>();
      this.prevHistoryStack = new Stack<>();
      this.nextHistoryStack = new Stack<String>();
   }

   void scheduleCommand(String command) {
      if (command.equalsIgnoreCase("stop")) {
         this.commandQueue.addFirst(command);
      } else {
         this.commandQueue.add(command);
      }

   }

   public void scheduleScriptCommands(ArrayList<String> scriptLine) {
      ListIterator<String> lines = scriptLine.listIterator(scriptLine.size());

      while (lines.hasPrevious()) {
         String line = lines.previous();
         this.commandQueue.addFirst(line);
      }

   }

   public boolean hasMoreCommands() {
      return this.commandQueue.size() != 0;
   }

   public String getNextCommand() {
      return this.commandQueue.removeFirst();
   }

   public boolean hasQueuedStop() {
      return (this.commandQueue.getFirst()).equalsIgnoreCase("stop");
   }

   public void addToHistory(String command) {
      if (this.prevHistoryStack.empty()) {
         this.prevHistoryStack.push(command);
      } else if (!this.prevHistoryStack.peek().equals(command)) {
         this.prevHistoryStack.push(command);
      }

   }

   public String getPrevHistory() {
      if (this.prevHistoryStack.empty()) {
         return null;
      } else {
         String command = this.prevHistoryStack.pop();
         this.nextHistoryStack.push(command);
         return command;
      }
   }

   public String getNextHistory() {
      if (this.nextHistoryStack.empty()) {
         return null;
      } else {
         String command = this.nextHistoryStack.pop();
         this.prevHistoryStack.push(command);
         return command;
      }
   }

   private void resetHistoryStack() {
      while (!this.nextHistoryStack.empty()) {
         this.prevHistoryStack.push(this.nextHistoryStack.pop());
      }

   }

   private void setupCommands() {
      this.commands.put("help", new CommandLine.Command() {
         public String getUsage() {
            return "h[elp] [command]";
         }

         public String getHelp() {
            return "Print out help for all available commands, or for just a specified command.";
         }

         public String doCommand(String[] argArray, int argSize) {
            if (argSize > 2) {
               return this.getUsage();
            } else if (argSize != 1) {
               CommandLine.Command var8 = CommandLine.this.commands
                       .get(argArray[1].toLowerCase());
               return var8 == null ? argArray[1] + ": command not found"
                       : "usage: " + var8.getUsage() + "\n   " + var8.getHelp();
            } else {
               String var3 = "";

               String var6;
               String var7;
               for (Iterator<Command> var4 = CommandLine.this.commandsSet.iterator();
                       var4.hasNext(); var3 = var3 + var7 + " usage: " + var6 + "\n") {
                  CommandLine.Command var5 = var4.next();
                  var6 = var5.getUsage();
                  var7 = var6.split("\\s+")[0];
               }

               return var3;
            }
         }
      });
      this.commands.put("h", this.commands.get("help"));
      this.commands.put("quit", new CommandLine.Command() {
         public String getUsage() {
            return "quit";
         }

         public String getHelp() {
            return "Quit the simulator.";
         }

         public String doCommand(String[] argArray, int argSize) {
            return argSize != 1 ? this.getUsage() : null;
         }
      });
      this.commands.put("q", this.commands.get("quit"));
      this.commands.put("exit", this.commands.get("quit"));
      this.commands.put("next", new CommandLine.Command() {
         public String getUsage() {
            return "n[ext]";
         }

         public String getHelp() {
            return "Executes the next instruction.";
         }

         public String doCommand(String[] argArray, int argSize) throws ExceptionException {
            if (argSize != 1) {
               return this.getUsage();
            } else {
               CommandLine.this.mac.executeNext();
               return "";
            }
         }
      });
      this.commands.put("n", this.commands.get("next"));
      this.commands.put("step", new CommandLine.Command() {
         public String getUsage() {
            return "s[tep]";
         }

         public String getHelp() {
            return "Steps into the next instruction.";
         }

         public String doCommand(String[] argArray, int argSize) throws ExceptionException {
            CommandLine.this.mac.executeStep();
            return "";
         }
      });
      this.commands.put("s", this.commands.get("step"));
      this.commands.put("continue", new CommandLine.Command() {
         public String getUsage() {
            return "c[ontinue]";
         }

         public String getHelp() {
            return "Continues running instructions until next breakpoint is hit.";
         }

         public String doCommand(String[] argArray, int argSize) throws ExceptionException {
            Console.println("use the 'stop' command to interrupt execution");
            CommandLine.this.mac.executeMany();
            return "";
         }
      });
      this.commands.put("c", this.commands.get("continue"));
      this.commands.put("stop", new CommandLine.Command() {
         public String getUsage() {
            return "stop";
         }

         public String getHelp() {
            return "Stops execution temporarily.";
         }

         public String doCommand(String[] argArray, int argSize) {
            return CommandLine.this.mac.stopExecution(true);
         }
      });
      this.commands.put("reset", new CommandLine.Command() {
         public String getUsage() {
            return "reset";
         }

         public String getHelp() {
            return "Resets the machine and simulator.";
         }

         public String doCommand(String[] argArray, int argSize) {
            if (argSize != 1) {
               return this.getUsage();
            } else {
               CommandLine.this.mac.stopExecution(false);
               CommandLine.this.mac.reset();
               CommandLine.this.checksPassed = 0;
               CommandLine.this.checksFailed = 0;
               return "System reset";
            }
         }
      });
      this.commands.put("print", new CommandLine.Command() {
         public String getUsage() {
            return "p[rint]";
         }

         public String getHelp() {
            return "Prints out all registers, PC, MPR and PSR.";
         }

         public String doCommand(String[] argArray, int argSize) {
            return argSize != 1 ? this.getUsage()
                    : CommandLine.this.mac.getRegisterFile().toString();
         }
      });
      this.commands.put("p", this.commands.get("print"));
      this.commands.put("input", new CommandLine.Command() {
         public String getUsage() {
            return "input <filename>";
         }

         public String getHelp() {
            return "Specifies a file to read the input from instead of keyboard device (simulator must be restarted to restore normal keyboard input).";
         }

         public String doCommand(String[] argArray, int argSize) {
            if (argSize != 2) {
               return this.getUsage();
            } else {
               File var3 = new File(argArray[1]);
               return var3.exists() ? CommandLine.this.mac.setKeyboardInputStream(var3)
                       : "Error: file " + argArray[1] + " does not exist.";
            }
         }
      });
      this.commands.put("break", new CommandLine.Command() {
         public String getUsage() {
            return "b[reak] [ set | clear ] [ mem_addr | label ]";
         }

         public String getHelp() {
            return "Sets or clears break point at specified memory address or label.";
         }

         public String doCommand(String[] argArray, int argSize) {
            if (argSize != 3) {
               return this.getUsage();
            } else if (argArray[1].toLowerCase().equals("set")) {
               return CommandLine.this.mac.getMemory().setBreakPoint(argArray[2]);
            } else {
               return argArray[1].toLowerCase().equalsIgnoreCase("clear")
                       ? CommandLine.this.mac.getMemory().clearBreakPoint(
                       argArray[2]) : this.getUsage();
            }
         }
      });
      this.commands.put("b", this.commands.get("break"));
      this.commands.put("script", new CommandLine.Command() {
         public String getUsage() {
            return "script <filename>";
         }

         public String getHelp() {
            return "Specifies a file from which to read commands.";
         }

         public String doCommand(String[] argArray, int argSize) {
            if (argSize != 2) {
               return this.getUsage();
            } else {
               File var3 = new File(argArray[1]);

               try {
                  BufferedReader var4 = new BufferedReader(new FileReader(var3));
                  ArrayList<String> var6 = new ArrayList<String>();

                  while (true) {
                     String var5 = var4.readLine();
                     if (var5 == null) {
                        CommandLine.this.scheduleScriptCommands(var6);
                        return "";
                     }

                     var6.add("@" + var5);
                  }
               } catch (IOException var7) {
                  return var7.getMessage();
               }
            }
         }
      });
      this.commands.put("load", new CommandLine.Command() {
         public String getUsage() {
            return "l[oa]d <filename>";
         }

         public String getHelp() {
            return "Loads an object file into the memory.";
         }

         public String doCommand(String[] argArray, int argSize) {
            return argSize != 2 ? this.getUsage()
                    : CommandLine.this.mac.loadObjectFile(new File(
                            argArray[1]));
         }
      });
      this.commands.put("ld", this.commands.get("load"));
      this.commands.put("check", new CommandLine.Command() {
         public String getUsage() {
            return "check [ count | cumulative | reset | PC | reg | PSR | MPR | mem_addr | label | N | Z | P ] [ mem_addr | label ] [ value | label ]";
         }

         public String getHelp() {
            String var1 = "Verifies that a particular value resides in a register or in a memory location, or that a condition code is set.\n";
            var1 = var1 + "Samples:\n";
            var1 = var1
                    + "'check PC LABEL' checks if the PC points to wherever LABEL points.\n";
            var1 = var1
                    + "'check LABEL VALUE' checks if the value stored in memory at the location pointed to by LABEL is equal to VALUE.\n";
            var1 = var1
                    + "'check VALUE LABEL' checks if the value stored in memory at VALUE is equal to the location pointed to by LABEL (probably not very useful). To find out where a label points, use 'list' instead.\n";
            return var1;
         }

         private String check(boolean var1, String[] argArray, String var3) {
            String strTrue = "TRUE";
            String strFalse = "FALSE";
            StringBuilder sb = new StringBuilder("(");

            for (int i = 0; i < argArray.length; i++) {
               sb.append(argArray[i]);
               sb.append(i == argArray.length - 1 ? ")" : " ");
            }

            if (var1) {
               CommandLine.this.checksPassed++;
               CommandLine.this.checksPassedCumulative++;
               return strTrue + " " + sb;
            } else {
               CommandLine.this.checksFailed++;
               CommandLine.this.checksFailedCumulative++;
               return strFalse + " " + sb + " (actual value: " + var3 + ")";
            }
         }

         public String doCommand(String[] argArray, int argSize) {
            if (argSize >= 2 && argSize <= 4) {
               if (argSize == 2) {
                  String var12;
                  if (argArray[1].equals("count")) {
                     var12 = CommandLine.this.checksPassed == 1 ? "check" : "checks";
                     return CommandLine.this.checksPassed + " " + var12 + " passed, "
                             + CommandLine.this.checksFailed + " failed";
                  } else if (argArray[1].equals("cumulative")) {
                     var12 = CommandLine.this.checksPassedCumulative == 1 ? "check"
                             : "checks";
                     return " -> " + CommandLine.this.checksPassedCumulative + " " + var12
                             + " passed, " + CommandLine.this.checksFailedCumulative
                             + " failed";
                  } else if (argArray[1].equals("reset")) {
                     CommandLine.this.checksPassed = 0;
                     CommandLine.this.checksFailed = 0;
                     CommandLine.this.checksPassedCumulative = 0;
                     CommandLine.this.checksFailedCumulative = 0;
                     return "check counts reset";
                  } else {
                     RegisterFile var11 = CommandLine.this.mac.getRegisterFile();
                     return (!argArray[1].toLowerCase().equals("n") || !var11.getN()) && (
                             !argArray[1].toLowerCase().equals("z") || !var11.getZ()) && (
                             !argArray[1].toLowerCase().equals("p") || !var11.getP()) ? this
                             .check(false,
                                     argArray, var11.printCC())
                             : this.check(true, argArray, "");
                  }
               } else {
                  int var3 = Word.parseNum(argArray[argSize - 1]);
                  if (var3 == Integer.MAX_VALUE) {
                     var3 = CommandLine.this.mac.lookupSym(argArray[argSize - 1]);
                     if (var3 == Integer.MAX_VALUE) {
                        return "Bad value or label: " + argArray[argSize - 1];
                     }
                  }

                  Boolean isRegister = CommandLine.this.checkRegister(argArray[1], var3);
                  if (isRegister != null) {
                     return this.check(isRegister, argArray,
                             CommandLine.this.getRegister(argArray[1]));
                  } else {
                     int var5 = CommandLine.this.mac.getAddress(argArray[1]);
                     if (var5 == Integer.MAX_VALUE) {
                        return "Bad register, value or label: " + argArray[1];
                     } else if (var5 >= 0 && var5 < 65536) {
                        int var6;
                        if (argSize == 3) {
                           var6 = var5;
                        } else {
                           var6 = CommandLine.this.mac.getAddress(argArray[2]);
                           if (var6 == Integer.MAX_VALUE) {
                              return "Bad register, value or label: " + argArray[2];
                           }

                           if (var6 < 0 || var6 >= 65536) {
                              return "Address " + argArray[2] + " out of bounds";
                           }

                           if (var6 < var5) {
                              return "Second address in range (" + argArray[2]
                                      + ") must be >= first (" + argArray[1] + ")";
                           }
                        }

                        Word var7 = null;
                        boolean var8 = true;
                        String var9 = "";

                        for (int var10 = var5; var10 <= var6; ++var10) {
                           var7 = CommandLine.this.mac.getMemory().read(var10);
                           if (var7 == null) {
                              return "Bad register, value or label: " + argArray[1];
                           }

                           if (var7.getValue() != (var3 & '\uffff')) {
                              var8 = false;
                              var9 = var9 + (var9.length() == 0 ? "" : ", ");
                              var9 = var9 + Word.toHex(var10) + ":" + var7.toHex();
                           }
                        }

                        return this.check(var8, argArray, var9);
                     } else {
                        return "Address " + argArray[1] + " out of bounds";
                     }
                  }
               }
            } else {
               return this.getUsage();
            }
         }
      });
      this.commands.put("dump", new CommandLine.Command() {
         public String getUsage() {
            return "d[ump] [-check | -coe | -readmemh | -disasm] from_mem_addr to_mem_addr dumpfile";
         }

         public String getHelp() {
            return "dumps a range of memory values to a specified file as raw values.\n  -check: dump as 'check' commands that can be run as an LC-3 script.\n  -coe dump a Xilinx coregen image\n  -readmemh dump a file readable by Verilog's $readmemh() system task.\n  -disasm dump disassembled instructions.";
         }

         public String doCommand(String[] argArray, int argSize) {
            byte var8 = 0;
            if (argSize >= 4 && argSize <= 5) {
               if (argSize == 5) {
                  if (argArray[1].equalsIgnoreCase("-check")) {
                     var8 = 1;
                  } else if (argArray[1].equalsIgnoreCase("-coe")) {
                     var8 = 2;
                  } else if (argArray[1].equalsIgnoreCase("-readmemh")) {
                     var8 = 3;
                  } else {
                     if (!argArray[1].equalsIgnoreCase("-disasm")) {
                        return "Unrecognized flag: " + argArray[1] + "\n" + this.getUsage();
                     }

                     var8 = 4;
                  }
               }

               int var9 = CommandLine.this.mac.getAddress(argArray[argSize - 3]);
               int var10 = CommandLine.this.mac.getAddress(argArray[argSize - 2]);
               if (var9 == Integer.MAX_VALUE) {
                  return "Error: Invalid register, address, or label  ('" + argArray[argSize
                          - 3] + "')";
               } else if (var9 >= 0 && var9 < 65536) {
                  if (var10 == Integer.MAX_VALUE) {
                     return "Error: Invalid register, address, or label  ('" + argArray[
                             argSize - 3] + "')";
                  } else if (var10 >= 0 && var10 < 65536) {
                     if (var10 < var9) {
                        return "Second address in range (" + argArray[argSize - 2]
                                + ") must be >= first (" + argArray[
                                argSize - 3] + ")";
                     } else {
                        Word var11 = null;
                        File var12 = new File(argArray[argSize - 1]);

                        PrintWriter var13;
                        try {
                           if (!var12.createNewFile()) {
                              return "File " + argArray[argSize - 1]
                                      + " already exists. Choose a different filename.";
                           }

                           var13 = new PrintWriter(
                                   new BufferedWriter(new FileWriter(var12)));
                        } catch (IOException var15) {
                           ErrorLog.logError(var15);
                           return "Error opening file: " + var12.getName();
                        }

                        if (var8 == 2) {
                           var13.println("MEMORY_INITIALIZATION_RADIX=2;");
                           var13.println("MEMORY_INITIALIZATION_VECTOR=");
                        }

                        for (int var14 = var9; var14 <= var10; ++var14) {
                           var11 = CommandLine.this.mac.getMemory().read(var14);
                           if (var11 == null) {
                              return "Bad register, value or label: " + argArray[argSize
                                      - 3];
                           }

                           switch (var8) {
                              case 0:
                                 var13.println(var11.toHex());
                                 break;
                              case 1:
                                 var13.println("check " + Word.toHex(var14) + " " + var11
                                         .toHex());
                                 break;
                              case 2:
                                 if (var14 < var10) {
                                    var13.println(var11.toBinary().substring(1) + ",");
                                 } else {
                                    var13.println(var11.toBinary().substring(1) + ";");
                                 }
                                 break;
                              case 3:
                                 var13.println(var11.toHex().substring(1));
                                 break;
                              case 4:
                                 var13.println(ISA.disassemble(var11, var14,
                                         CommandLine.this.mac));
                                 break;
                              default:
                                 assert false : "Invalid flag to `dump' command: "
                                         + argArray[1];
                           }
                        }

                        var13.close();
                        return "com.pennsim.Memory dumped.";
                     }
                  } else {
                     return "Address " + argArray[argSize - 2] + " out of bounds";
                  }
               } else {
                  return "Address " + argArray[argSize - 3] + " out of bounds";
               }
            } else {
               return this.getUsage();
            }
         }
      });
      this.commands.put("d", this.commands.get("dump"));
      this.commands.put("trace", new CommandLine.Command() {
         public String getUsage() {
            return "trace [on <trace-file> | off]";
         }

         public String getHelp() {
            return "For each instruction executed, this command dumps a subset of processor state to a file, to create a trace that can be used to verify correctness of execution. The state consists of, in order, (1) PC, (2) current insn, (3) regfile write-enable, (4) regfile data in, (5) data memory write-enable, (6) data memory address, and (7) data memory data in. These values are written in hex to <trace-file>, one line for each instruction executed. Note that trace files can get very large very quickly!\n   Sometimes a signal may be a don't-care value - if we're not writing to the regfile, the `regfile data in' value is undefined - but the write-enable values should allow don't-care signals to be determined in all cases.";
         }

         public String doCommand(String[] argArray, int argSize) {
            if (argSize >= 2 && argSize <= 3) {
               if (argSize == 3) {
                  if (!argArray[1].equalsIgnoreCase("on")) {
                     return this.getUsage();
                  } else if (CommandLine.this.mac.isTraceEnabled()) {
                     return "Tracing is already on.";
                  } else {
                     File var3 = new File(argArray[argSize - 1]);

                     PrintWriter var4;
                     try {
                        if (!var3.createNewFile()) {
                           return "File " + argArray[argSize - 1] + " already exists.";
                        }

                        var4 = new PrintWriter(new BufferedWriter(new FileWriter(var3)),
                                true);
                     } catch (IOException var6) {
                        ErrorLog.logError(var6);
                        return "Error opening file: " + var3.getName();
                     }

                     CommandLine.this.mac.setTraceWriter(var4);
                     return "Tracing is on.";
                  }
               } else {
                  assert argSize == 2;

                  if (!argArray[1].equalsIgnoreCase("off")) {
                     return this.getUsage();
                  } else if (!CommandLine.this.mac.isTraceEnabled()) {
                     return "Tracing is already off.";
                  } else {
                     CommandLine.this.mac.getTraceWriter().flush();
                     CommandLine.this.mac.getTraceWriter().close();
                     CommandLine.this.mac.disableTrace();
                     return "Tracing is off.";
                  }
               }
            } else {
               return this.getUsage();
            }
         }
      });
      this.commands.put("counters", new CommandLine.Command() {
         public String getUsage() {
            return "counters";
         }

         public String getHelp() {
            return "Print out values of internal performance counters.";
         }

         public String doCommand(String[] argArray, int argSize) {
            if (argSize != 1) {
               return this.getUsage();
            } else {
               String var3 = "Cycle count: " + CommandLine.this.mac.CYCLE_COUNT
                       + "\n";
               var3 = var3 + "com.pennsim.Instruction count: "
                       + CommandLine.this.mac.INSTRUCTION_COUNT
                       + "\n";
               var3 = var3 + "Load stall count: " + CommandLine.this.mac.LOAD_STALL_COUNT
                       + "\n";
               var3 = var3 + "Branch stall count: " + CommandLine.this.mac.BRANCH_STALL_COUNT
                       + "\n";
               return var3;
            }
         }
      });
      this.commands.put("set", new CommandLine.Command() {
         public String getUsage() {
            return "set [ PC | reg | PSR | MPR | mem_addr | label ] [ mem_addr | label ] [ value | N | Z | P ]";
         }

         public String getHelp() {
            return "Sets the value of a register/PC/PSR/label/memory location/memory range or set the condition codes individually.";
         }

         public String doCommand(String[] argArray, int argSize) {
            if (argSize >= 2 && argSize <= 4) {
               if (argSize == 2) {
                  String var7 = CommandLine.this.setConditionCodes(argArray[1]);
                  return var7 == null ? this.getUsage() : var7;
               } else {
                  int var3 = Word.parseNum(argArray[argSize - 1]);
                  if (var3 == Integer.MAX_VALUE) {
                     var3 = CommandLine.this.mac.lookupSym(argArray[argSize - 1]);
                  }

                  if (var3 == Integer.MAX_VALUE) {
                     return "Error: Invalid value (" + argArray[argSize - 1] + ")";
                  } else {
                     if (argSize == 3) {
                        String var4 = CommandLine.this.setRegister(argArray[1], var3);
                        if (var4 != null) {
                           return var4;
                        }
                     }

                     int var8 = CommandLine.this.mac.getAddress(argArray[1]);
                     if (var8 == Integer.MAX_VALUE) {
                        return "Error: Invalid register, address, or label  ('"
                                + argArray[1] + "')";
                     } else if (var8 >= 0 && var8 < 65536) {
                        int var5;
                        if (argSize == 3) {
                           var5 = var8;
                        } else {
                           var5 = CommandLine.this.mac.getAddress(argArray[2]);
                           if (var5 == Integer.MAX_VALUE) {
                              return "Error: Invalid register, address, or label  ('"
                                      + argArray[1] + "')";
                           }

                           if (var5 < 0 || var5 >= 65536) {
                              return "Address " + argArray[2] + " out of bounds";
                           }

                           if (var5 < var8) {
                              return "Second address in range (" + argArray[2]
                                      + ") must be >= first (" + argArray[1] + ")";
                           }
                        }

                        for (int var6 = var8; var6 <= var5; ++var6) {
                           CommandLine.this.mac.getMemory().write(var6, var3);
                        }

                        return argSize == 3 ? "com.pennsim.Memory location " + Word.toHex(var8)
                                + " updated to " + argArray[
                                argSize - 1]
                                : "com.pennsim.Memory locations " + Word.toHex(var5) + " to " + Word
                                        .toHex(var5) + " updated to " + argArray[
                                        argSize - 1];
                     } else {
                        return "Address " + argArray[1] + " out of bounds";
                     }
                  }
               }
            } else {
               return this.getUsage();
            }
         }
      });
      this.commands.put("list", new CommandLine.Command() {
         public String getUsage() {
            return "l[ist] [ addr1 | label1 [addr2 | label2] ]";
         }

         public String getHelp() {
            return "Lists the contents of memory locations (default address is PC. Specify range by giving 2 arguments).";
         }

         public String doCommand(String[] argArray, int argSize) {
            if (argSize > 3) {
               return this.getUsage();
            } else if (argSize == 1) {
               CommandLine.this.scrollToPC();
               return Word.toHex(CommandLine.this.mac.getRegisterFile().getPC()) + " : "
                       + CommandLine.this.mac.getMemory()
                       .getInst(CommandLine.this.mac.getRegisterFile().getPC()).toHex() + " : "
                       + ISA.disassemble(CommandLine.this.mac.getMemory()
                               .getInst(CommandLine.this.mac.getRegisterFile().getPC()),
                       CommandLine.this.mac.getRegisterFile().getPC(), CommandLine.this.mac);
            } else {
               int var4;
               if (argSize == 2) {
                  String var7 = CommandLine.this.getRegister(argArray[1]);
                  if (var7 != null) {
                     return argArray[1] + " : " + var7;
                  } else {
                     var4 = CommandLine.this.mac.getAddress(argArray[1]);
                     if (var4 == Integer.MAX_VALUE) {
                        return "Error: Invalid address or label (" + argArray[1] + ")";
                     } else {
                        if (PennSim.GRAPHICAL_MODE && var4 < 65024) {
                           CommandLine.this.GUI.scrollToIndex(var4);
                        }

                        return Word.toHex(var4) + " : " + CommandLine.this.mac.getMemory()
                                .read(var4).toHex() + " : " + ISA
                                .disassemble(CommandLine.this.mac.getMemory().read(var4),
                                        var4, CommandLine.this.mac);
                     }
                  }
               } else {
                  int var3 = CommandLine.this.mac.getAddress(argArray[1]);
                  var4 = CommandLine.this.mac.getAddress(argArray[2]);
                  if (var3 == Integer.MAX_VALUE) {
                     return "Error: Invalid address or label (" + argArray[1] + ")";
                  } else if (var4 == Integer.MAX_VALUE) {
                     return "Error: Invalid address or label (" + argArray[2] + ")";
                  } else if (var4 < var3) {
                     return "Error: addr2 should be larger than addr1";
                  } else {
                     StringBuffer var5 = new StringBuffer();

                     for (int var6 = var3; var6 <= var4; ++var6) {
                        var5.append(
                                Word.toHex(var6) + " : " + CommandLine.this.mac.getMemory()
                                        .read(var6).toHex() + " : " + ISA.disassemble(
                                        CommandLine.this.mac.getMemory().read(var6), var6,
                                        CommandLine.this.mac));
                        if (var6 != var4) {
                           var5.append("\n");
                        }
                     }

                     if (PennSim.GRAPHICAL_MODE) {
                        CommandLine.this.GUI.scrollToIndex(var3);
                     }

                     return new String(var5);
                  }
               }
            }
         }
      });
      this.commands.put("l", this.commands.get("list"));
      this.commands.put("as", new CommandLine.Command() {
         public String getUsage() {
            return "as [-warn] <filename>";
         }

         public String getHelp() {
            return "Assembles <filename> showing errors and (optionally) warnings, and leaves a .obj file in the same directory.";
         }

         public String doCommand(String[] argArray, int argSize) {
            if (argSize >= 2 && argSize <= 3) {
               String[] var3 = new String[argSize - 1];
               String var4 = "";
               var3[0] = argArray[1];
               var4 = var4 + argArray[1];
               if (argSize == 3) {
                  var3[1] = argArray[2];
                  var4 = var4 + " " + argArray[2];
               }

               Assembler var5 = new Assembler();
               String var6 = "";

               try {
                  var6 = var5.as(var3);
                  if (var6.length() != 0) {
                     return var6 + "Warnings encountered during assembly "
                             + "(but assembly completed w/o errors).";
                  }
               } catch (AsException var8) {
                  return var8.getMessage() + "\nErrors encountered during assembly.";
               }

               return "Assembly of '" + var4 + "' completed without errors or warnings.";
            } else {
               return this.getUsage();
            }
         }
      });
      this.commands.put("clear", new CommandLine.Command() {
         public String getUsage() {
            return "clear";
         }

         public String getHelp() {
            return "Clears the commandline output window. Available only in com.pennsim.GUI mode.";
         }

         public String doCommand(String[] argArray, int argSize) {
            if (PennSim.GRAPHICAL_MODE) {
               Console.clear();
               return "";
            } else {
               return "Error: clear is only available in com.pennsim.GUI mode";
            }
         }
      });
   }

   public String runCommand(String var1) throws ExceptionException, NumberFormatException {
      if (var1 == null) {
         return "";
      } else {
         if (!var1.startsWith("@")) {
            this.resetHistoryStack();
            this.addToHistory(var1);
         } else {
            var1 = var1.replaceFirst("^@", "");
         }

         String[] var2 = var1.split("\\s+");
         int var3 = var2.length;
         if (var3 == 0) {
            return "";
         } else {
            String var4 = var2[0].toLowerCase();
            if (var4.equals("")) {
               return "";
            } else {
               int var5 = -1;

               for (int var6 = 0; var6 < var2.length; ++var6) {
                  if (var2[var6].startsWith("#")) {
                     var5 = var6;
                     break;
                  }
               }

               if (var5 == 0) {
                  return "";
               } else {
                  if (var5 >= 0) {
                     String[] var8 = new String[var5];

                     for (int var7 = 0; var7 < var5; ++var7) {
                        var8[var7] = var2[var7];
                     }

                     var2 = var8;
                     var3 = var8.length;
                  }

                  CommandLine.Command var9 = this.commands.get(var4);
                  return var9 == null ? "Unknown command: " + var4
                          : var9.doCommand(var2, var3);
               }
            }
         }
      }
   }

   public void scrollToPC() {
      if (PennSim.GRAPHICAL_MODE) {
         this.GUI.scrollToPC();
      }

   }

   public String setRegister(String var1, int var2) {
      String var3 = "Register " + var1.toUpperCase() + " updated to value " + Word.toHex(var2);
      if (var1.equalsIgnoreCase("pc")) {
         this.mac.getRegisterFile().setPC(var2);
         this.scrollToPC();
      } else if (var1.equalsIgnoreCase("psr")) {
         this.mac.getRegisterFile().setPSR(var2);
      } else if (var1.equalsIgnoreCase("mpr")) {
         Memory var10000 = this.mac.getMemory();
         this.mac.getMemory();
         var10000.write(65042, var2);
      } else if ((var1.startsWith("r") || var1.startsWith("R")) && var1.length() == 2) {
         Integer var4 = new Integer(var1.substring(1, 2));
         this.mac.getRegisterFile().setRegister(var4, var2);
      } else {
         var3 = null;
      }

      return var3;
   }

   public String setConditionCodes(String var1) {
      String var2 = null;
      if (var1.equalsIgnoreCase("n")) {
         this.mac.getRegisterFile().setN();
         var2 = "PSR N bit set";
      } else if (var1.equalsIgnoreCase("z")) {
         this.mac.getRegisterFile().setZ();
         var2 = "PSR Z bit set";
      } else if (var1.equalsIgnoreCase("p")) {
         this.mac.getRegisterFile().setP();
         var2 = "PSR P bit set";
      }

      return var2;
   }

   public String getRegister(String var1) {
      int var2;
      if (var1.equalsIgnoreCase("pc")) {
         var2 = this.mac.getRegisterFile().getPC();
      } else if (var1.equalsIgnoreCase("psr")) {
         var2 = this.mac.getRegisterFile().getPSR();
      } else if (var1.equalsIgnoreCase("mpr")) {
         var2 = this.mac.getRegisterFile().getMPR();
      } else {
         if (!var1.startsWith("r") && !var1.startsWith("R") || var1.length() != 2) {
            return null;
         }

         Integer var3 = new Integer(var1.substring(1, 2));
         var2 = this.mac.getRegisterFile().getRegister(var3);
      }

      return Word.toHex(var2);
   }

   private Boolean checkRegister(String register, int value) {
      int num = Word.parseNum(this.getRegister(register));
      if (num == Integer.MAX_VALUE) {
         return null;
      } else {
         Word word = new Word(value);
         return (num == word.getValue());
      }
   }

   public void reset() {
      this.checksPassed = 0;
      this.checksFailed = 0;
   }

   void setGUI(GUI gui) {
      this.GUI = gui;
   }

   private interface Command {

      String getUsage();

      String getHelp();

      String doCommand(String[] argArray, int argSize) throws ExceptionException;
   }
}
