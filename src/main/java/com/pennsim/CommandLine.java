package com.pennsim;

import com.pennsim.exception.AsException;
import com.pennsim.exception.GenericException;
import com.pennsim.gui.Console;
import com.pennsim.gui.GUI;
import com.pennsim.util.ErrorLog;
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

    static final String PROMPT;
    private static final String NEWLINE = System.getProperty("line.separator");

    static {
        PROMPT = NEWLINE + ">> ";
    }

    private Machine mac;
    private com.pennsim.gui.GUI GUI;
    private LinkedList<String> commandQueue;
    private Stack<String> prevHistoryStack;
    private Stack<String> nextHistoryStack;
    private Hashtable<String, Command> commands;
    private TreeSet<Command> commandsSet;
    private int checksPassed = 0;
    private int checksFailed = 0;
    private int checksPassedCumulative = 0;
    private int checksFailedCumulative = 0;

    CommandLine(Machine machine) {
        this.mac = machine;
        this.commands = new Hashtable<>();
        this.setupCommands();
      /*
 TODO: Resolve unchecked assignment error
         public boolean equals(Object var1) {
            CommandLine.Command var2 = (CommandLine.Command)var1;
            String var3 = var2.getUsage().split("\\s+")[0];
            String var4 = ((CommandLine.Command)this).getUsage().split("\\s+")[0];
            return var3.equals(var4);
         }
*/
        this.commandsSet = new TreeSet<Command>((Comparator) (var1, var2) -> {
            Command var3 = (Command) var1;
            Command var4 = (Command) var2;
            String var5 = var3.getUsage().split("\\s+")[0];
            String var6 = var4.getUsage().split("\\s+")[0];
            return var5.compareTo(var6);
        });
        this.commandsSet.addAll(this.commands.values());
        this.commandQueue = new LinkedList<>();
        this.prevHistoryStack = new Stack<>();
        this.nextHistoryStack = new Stack<>();
    }

    /**
     * Schedule a command in the command queue
     *
     * @param command the command to be scheduled
     */
    public void scheduleCommand(String command) {
        if (command.equalsIgnoreCase("stop")) {
            this.commandQueue.addFirst(command);
        } else {
            this.commandQueue.add(command);
        }

    }

    /**
     * Schedule the commands of a script into the command queue
     *
     * @param scriptLine script commands to be scheduled
     */
    private void scheduleScriptCommands(ArrayList<String> scriptLine) {
        ListIterator<String> lines = scriptLine.listIterator(scriptLine.size());

        while (lines.hasPrevious()) {
            String line = lines.previous();
            this.commandQueue.addFirst(line);
        }

    }

    /**
     * See if the command queue has more commands in it
     *
     * @return if the command queue has more commands
     */
    public boolean hasMoreCommands() {
        return this.commandQueue.size() != 0;
    }

    /**
     * Get the next command
     *
     * @return the next command in the command queue
     */
    public String getNextCommand() {
        return this.commandQueue.removeFirst();
    }

    /**
     * return if the stop command has been queued
     *
     * @return if the stop command has been queued
     */
    public boolean hasQueuedStop() {
        return (this.commandQueue.getFirst()).equalsIgnoreCase("stop");
    }

    /**
     * Add a command to the command history stack
     *
     * @param command the command to be added to the stack
     */
    private void addToHistory(String command) {
        if (this.prevHistoryStack.empty()) {
            this.prevHistoryStack.push(command);
        } else if (!this.prevHistoryStack.peek().equals(command)) {
            this.prevHistoryStack.push(command);
        }

    }

    /**
     * Get the most recent command from history and add it to the next history stack
     *
     * @return the top command on the command previous history stack
     */
    public String getPrevHistory() {
        if (this.prevHistoryStack.empty()) {
            return null;
        } else {
            String command = this.prevHistoryStack.pop();
            this.nextHistoryStack.push(command);
            return command;
        }
    }

    /**
     * Get the most recent command from the next history stack and put it in the previous history
     * stack
     *
     * @return the top command on the command next history stack
     */
    public String getNextHistory() {
        if (this.nextHistoryStack.empty()) {
            return null;
        } else {
            String command = this.nextHistoryStack.pop();
            this.prevHistoryStack.push(command);
            return command;
        }
    }

    /**
     * Reset the history stacl
     */
    private void resetHistoryStack() {
        while (!this.nextHistoryStack.empty()) {
            this.prevHistoryStack.push(this.nextHistoryStack.pop());
        }

    }

    /**
     * Set up all the commands for the program
     */
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
                    CommandLine.Command command = CommandLine.this.commands
                            .get(argArray[1].toLowerCase());
                    return command == null ? argArray[1] + ": command not found"
                            : "usage: " + command.getUsage() + "\n   " + command.getHelp();
                } else {
                    String result = "";

                    String usage;
                    String var7;
                    for (Iterator<Command> iterator = CommandLine.this.commandsSet.iterator();
                            iterator.hasNext(); result = result + var7 + " usage: " + usage + "\n") {
                        CommandLine.Command nextCommand = iterator.next();
                        usage = nextCommand.getUsage();
                        var7 = usage.split("\\s+")[0];
                    }

                    return result;
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

            public String doCommand(String[] argArray, int argSize) throws GenericException {
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

            public String doCommand(String[] argArray, int argSize) throws GenericException {
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

            public String doCommand(String[] argArray, int argSize) throws GenericException {
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
                    File file = new File(argArray[1]);
                    return file.exists() ? CommandLine.this.mac.setKeyboardInputStream(file)
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
                    File file = new File(argArray[1]);

                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        ArrayList<String> scriptLines = new ArrayList<>();

                        while (true) {
                            String line = reader.readLine();
                            if (line == null) {
                                CommandLine.this.scheduleScriptCommands(scriptLines);
                                return "";
                            }

                            scriptLines.add("@" + line);
                        }
                    } catch (IOException e) {
                        return e.getMessage();
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
                return "Verifies that a particular value resides in a register or in a memory location, or that a condition code is set.\n"
                        + "Samples:\n"
                        + "'check PC LABEL' checks if the PC points to wherever LABEL points.\n"
                        + "'check LABEL VALUE' checks if the value stored in memory at the location pointed to by LABEL is equal to VALUE.\n"
                        + "'check VALUE LABEL' checks if the value stored in memory at VALUE is equal to the location pointed to by LABEL (probably not very useful). To find out where a label points, use 'list' instead.\n";
            }

            private String check(boolean expectedResult, String[] argArray, String value) {
                StringBuilder builder = new StringBuilder("(");

                for (int i = 0; i < argArray.length; i++) {
                    builder.append(argArray[i]);
                    builder.append(i == argArray.length - 1 ? ")" : " ");
                }

                if (expectedResult) {
                    CommandLine.this.checksPassed++;
                    CommandLine.this.checksPassedCumulative++;
                    return "TRUE " + builder.toString();
                } else {
                    CommandLine.this.checksFailed++;
                    CommandLine.this.checksFailedCumulative++;
                    return "FALSE " + builder.toString() + " (actual value: " + value + ")";
                }
            }

            public String doCommand(String[] argArray, int argSize) {
                if (argSize >= 2 && argSize <= 4) {
                    if (argSize == 2) {
                        String var12;
                        switch (argArray[1]) {
                            case "count":
                                var12 = CommandLine.this.checksPassed == 1 ? "check" : "checks";
                                return CommandLine.this.checksPassed + " " + var12 + " passed, "
                                        + CommandLine.this.checksFailed + " failed";
                            case "cumulative":
                                var12 = CommandLine.this.checksPassedCumulative == 1 ? "check"
                                        : "checks";
                                return " -> " + CommandLine.this.checksPassedCumulative + " " + var12
                                        + " passed, " + CommandLine.this.checksFailedCumulative
                                        + " failed";
                            case "reset":
                                CommandLine.this.checksPassed = 0;
                                CommandLine.this.checksFailed = 0;
                                CommandLine.this.checksPassedCumulative = 0;
                                CommandLine.this.checksFailedCumulative = 0;
                                return "check counts reset";
                            default:
                                RegisterFile registerFile = CommandLine.this.mac.getRegisterFile();
                                return (!argArray[1].toLowerCase().equals("n") || !registerFile.getN()) && (
                                        !argArray[1].toLowerCase().equals("z") || !registerFile.getZ()) && (
                                        !argArray[1].toLowerCase().equals("p") || !registerFile.getP())
                                        ? this
                                        .check(false,
                                                argArray, registerFile.printCC())
                                        : this.check(true, argArray, "");
                        }
                    } else {
                        int value = Word.parseNum(argArray[argSize - 1]);
                        if (value == Integer.MAX_VALUE) {
                            value = CommandLine.this.mac.lookupSym(argArray[argSize - 1]);
                            if (value == Integer.MAX_VALUE) {
                                return "Bad value or label: " + argArray[argSize - 1];
                            }
                        }

                        Boolean isRegister = CommandLine.this.checkRegister(argArray[1], value);
                        if (isRegister != null) {
                            return this.check(isRegister, argArray,
                                    CommandLine.this.getRegister(argArray[1]));
                        } else {
                            int startAddress = CommandLine.this.mac.getAddress(argArray[1]);
                            if (startAddress == Integer.MAX_VALUE) {
                                return "Bad register, value or label: " + argArray[1];
                            } else if (startAddress >= 0 && startAddress < Memory.MEM_SIZE) {
                                int endAddress;
                                if (argSize == 3) {
                                    endAddress = startAddress;
                                } else {
                                    endAddress = CommandLine.this.mac.getAddress(argArray[2]);
                                    if (endAddress == Integer.MAX_VALUE) {
                                        return "Bad register, value or label: " + argArray[2];
                                    }

                                    if (endAddress < 0 || endAddress >= Memory.MEM_SIZE) {
                                        return "Address " + argArray[2] + " out of bounds";
                                    }

                                    if (endAddress < startAddress) {
                                        return "Second address in range (" + argArray[2]
                                                + ") must be >= first (" + argArray[1] + ")";
                                    }
                                }

                                Word word;
                                boolean expectedResult = true;
                                StringBuilder builder = new StringBuilder();

                                for (int address = startAddress; address <= endAddress; ++address) {
                                    word = CommandLine.this.mac.getMemory().read(address);
                                    if (word == null) {
                                        return "Bad register, value or label: " + argArray[1];
                                    }

                                    if (word.getValue() != (value & '\uffff')) {
                                        expectedResult = false;
                                        builder.append(builder.length() == 0 ? "" : ", ");
                                        builder.append(Word.toHex(address)).append(":").append(word.toHex());
                                    }
                                }

                                return this.check(expectedResult, argArray, builder.toString());
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
                byte flag = 0;
                if (argSize >= 4 && argSize <= 5) {
                    if (argSize == 5) {
                        if (argArray[1].equalsIgnoreCase("-check")) {
                            flag = 1;
                        } else if (argArray[1].equalsIgnoreCase("-coe")) {
                            flag = 2;
                        } else if (argArray[1].equalsIgnoreCase("-readmemh")) {
                            flag = 3;
                        } else {
                            if (!argArray[1].equalsIgnoreCase("-disasm")) {
                                return "Unrecognized flag: " + argArray[1] + "\n" + this.getUsage();
                            }

                            flag = 4;
                        }
                    }

                    int startAddress = CommandLine.this.mac.getAddress(argArray[argSize - 3]);
                    int endAddress = CommandLine.this.mac.getAddress(argArray[argSize - 2]);
                    if (startAddress == Integer.MAX_VALUE) {
                        return "Error: Invalid register, address, or label  ('" + argArray[argSize - 3] + "')";
                    } else if (startAddress >= 0 && startAddress < Memory.MEM_SIZE) {
                        if (endAddress == Integer.MAX_VALUE) {
                            return "Error: Invalid register, address, or label  ('" + argArray[argSize - 3] + "')";
                        } else if (endAddress >= 0 && endAddress < Memory.MEM_SIZE) {
                            if (endAddress < startAddress) {
                                return "Second address in range (" + argArray[argSize - 2] + ") must be >= first ("
                                        + argArray[argSize - 3] + ")";
                            } else {
                                Word word;
                                File file = new File(argArray[argSize - 1]);

                                PrintWriter writer;
                                try {
                                    if (!file.createNewFile()) {
                                        return "File " + argArray[argSize - 1]
                                                + " already exists. Choose a different filename.";
                                    }

                                    writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
                                } catch (IOException e) {
                                    ErrorLog.logError(e);
                                    return "Error opening file: " + file.getName();
                                }

                                if (flag == 2) {
                                    writer.println("MEMORY_INITIALIZATION_RADIX=2;");
                                    writer.println("MEMORY_INITIALIZATION_VECTOR=");
                                }

                                for (int address = startAddress; address <= endAddress; ++address) {
                                    word = CommandLine.this.mac.getMemory().read(address);
                                    if (word == null) {
                                        return "Bad register, value or label: " + argArray[argSize - 3];
                                    }

                                    switch (flag) {
                                        case 0:
                                            writer.println(word.toHex());
                                            break;
                                        case 1:
                                            writer.println("check " + Word.toHex(address) + " " + word.toHex());
                                            break;
                                        case 2:
                                            if (address < endAddress) {
                                                writer.println(word.toBinary().substring(1) + ",");
                                            } else {
                                                writer.println(word.toBinary().substring(1) + ";");
                                            }
                                            break;
                                        case 3:
                                            writer.println(word.toHex().substring(1));
                                            break;
                                        case 4:
                                            writer.println(ISA.disassemble(word, address, CommandLine.this.mac));
                                            break;
                                        default:
                                            assert false : "Invalid flag to `dump' command: " + argArray[1];
                                            break;
                                    }
                                }

                                writer.close();
                                return "Memory dumped.";
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
                            File file = new File(argArray[argSize - 1]);

                            PrintWriter writer;
                            try {
                                if (!file.createNewFile()) {
                                    return "File " + argArray[argSize - 1] + " already exists.";
                                }

                                writer = new PrintWriter(new BufferedWriter(new FileWriter(file)), true);
                            } catch (IOException e) {
                                ErrorLog.logError(e);
                                return "Error opening file: " + file.getName();
                            }

                            CommandLine.this.mac.setTraceWriter(writer);
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
                    String counts = "Cycle count: " + CommandLine.this.mac.cycleCount
                            + "\n";
                    counts = counts + "Instruction count: "
                            + CommandLine.this.mac.instructionCount
                            + "\n";
                    counts = counts + "Load stall count: " + CommandLine.this.mac.loadStallCount
                            + "\n";
                    counts = counts + "Branch stall count: " + CommandLine.this.mac.branchStallCount
                            + "\n";
                    return counts;
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

                                return argSize == 3 ? "Memory location " + Word.toHex(var8)
                                        + " updated to " + argArray[
                                        argSize - 1]
                                        : "Memory locations " + Word.toHex(var5) + " to " + Word
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
                            .getInstruction(CommandLine.this.mac.getRegisterFile().getPC()).toHex() + " : "
                            + ISA.disassemble(CommandLine.this.mac.getMemory()
                                    .getInstruction(CommandLine.this.mac.getRegisterFile().getPC()),
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
                                var5.append(Word.toHex(var6)).append(" : ")
                                        .append(CommandLine.this.mac.getMemory().read(var6).toHex())
                                        .append(" : ")
                                        .append(ISA.disassemble(CommandLine.this.mac.getMemory()
                                                .read(var6), var6, CommandLine.this.mac));
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
                    // Take all arguments after the "as" or "assemble"
                    //    command and put them in a new array.
                    String[] asArgs = new String[argSize - 1];
                    String asArg = "";
                    asArgs[0] = argArray[1];
                    asArg = asArg + argArray[1];
                    // If the "-warn" argument is used then this is
                    if (argSize == 3) {
                        asArgs[1] = argArray[2];
                        asArg = asArg + " " + argArray[2];
                    }

                    Assembler assembler = new Assembler();
                    String asOutput; // Will remain empty if assembly completes successfully without warnings

                    try {
                        asOutput = assembler.as(asArgs);

                        if (asOutput.length() != 0) {
                            return asOutput + "Warnings encountered during assembly "
                                    + "(but assembly completed w/o errors).";
                        }
                    } catch (AsException e) {
                        return e.getMessage() + "\nErrors encountered during assembly.";
                    }

                    return "Assembly of '" + asArg + "' completed without errors or warnings.";
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
                return "Clears the commandline output window. Available only in GUI mode.";
            }

            public String doCommand(String[] argArray, int argSize) {
                if (PennSim.GRAPHICAL_MODE) {
                    Console.clear();
                    return "";
                } else {
                    return "Error: clear is only available in GUI mode";
                }
            }
        });
        this.commands.put("loadassembly", new CommandLine.Command() {
            public String getUsage() {
                return "l[oa]da[ssembly] [-warn] <filename>";
            }

            public String getHelp() {
                return "Assembles <filename> showing errors and (optionally) warnings, and then loads the .obj file into memory.";
            }

            public String doCommand(String[] argArray, int argSize) {
                return "Error: This command has not been setup.";
            }
        });
        this.commands.put("lda", this.commands.get("loadassembly"));
    }

    public String runCommand(String input) throws GenericException, NumberFormatException {
        if (input != null) {
            if (!input.startsWith("@")) {
                this.resetHistoryStack();
                this.addToHistory(input);
            } else {
                input = input.replaceFirst("^@", "");
            }

            String[] tokens = input.split("\\s+");
            int size = tokens.length;
            if (size == 0) {
                return "";
            } else {
                String token = tokens[0].toLowerCase();
                if (token.equals("")) {
                    return "";
                } else {
                    int tokenNum = -1;

                    for (int i = 0; i < tokens.length; ++i) {
                        if (tokens[i].startsWith("#")) {
                            tokenNum = i;
                            break;
                        }
                    }

                    if (tokenNum == 0) {
                        return "";
                    } else {
                        if (tokenNum >= 0) {
                            String[] newTokens = new String[tokenNum];

                            System.arraycopy(tokens, 0, newTokens, 0, tokenNum);

                            tokens = newTokens;
                            size = newTokens.length;
                        }

                        Command command = this.commands.get(token);
                        return command == null ? "Unknown command: " + token : command.doCommand(tokens, size);
                    }
                }
            }
        } else {
            return "";
        }
    }

    private void scrollToPC() {
        if (PennSim.GRAPHICAL_MODE) {
            this.GUI.scrollToPC();
        }

    }

    private String setRegister(String register, int value) {
        String output = "Register " + register.toUpperCase() + " updated to value " + Word.toHex(value);
        if (register.equalsIgnoreCase("pc")) {
            this.mac.getRegisterFile().setPC(value);
            this.scrollToPC();
        } else if (register.equalsIgnoreCase("psr")) {
            this.mac.getRegisterFile().setPSR(value);
        } else if (register.equalsIgnoreCase("mpr")) {
            Memory memory = this.mac.getMemory();
            memory.write(65042, value);
        } else if ((register.startsWith("r") || register.startsWith("R")) && register.length() == 2) {
            int registerValue = Integer.parseInt(register.substring(1, 2));
            this.mac.getRegisterFile().setRegister(registerValue, value);
        } else {
            output = null;
        }

        return output;
    }

    /**
     * This function is used to set condition codes
     *
     * @param conditionCode the code to be set
     * @return a String stating that the code was set
     */
    private String setConditionCodes(String conditionCode) {
        String result = null;
        if (conditionCode.equalsIgnoreCase("n")) {
            this.mac.getRegisterFile().setN();
            result = "PSR N bit set";
        } else if (conditionCode.equalsIgnoreCase("z")) {
            this.mac.getRegisterFile().setZ();
            result = "PSR Z bit set";
        } else if (conditionCode.equalsIgnoreCase("p")) {
            this.mac.getRegisterFile().setP();
            result = "PSR P bit set";
        }

        return result;
    }

    /**
     * Get the value of the inputted register
     *
     * @param register register to check
     * @return value of the register you checked
     */
    private String getRegister(String register) {
        int registerValue;
        if (register.equalsIgnoreCase("pc")) {
            registerValue = this.mac.getRegisterFile().getPC();
        } else if (register.equalsIgnoreCase("psr")) {
            registerValue = this.mac.getRegisterFile().getPSR();
        } else if (register.equalsIgnoreCase("mpr")) {
            registerValue = this.mac.getRegisterFile().getMPR();
        } else {
            if (!register.startsWith("r") && !register.startsWith("R") || register.length() != 2) {
                return null;
            }

            int var3 = Integer.parseInt(register.substring(1, 2));
            registerValue = this.mac.getRegisterFile().getRegister(var3);
        }

        return Word.toHex(registerValue);
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

    public void setGUI(GUI gui) {
        this.GUI = gui;
    }

    private interface Command {

        String getUsage();

        String getHelp();

        String doCommand(String[] argArray, int argSize) throws GenericException;
    }
}
