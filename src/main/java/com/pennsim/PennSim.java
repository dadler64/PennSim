package com.pennsim;

import com.adlerd.logger.Logger;
import com.pennsim.gui.start.Startup;

public class PennSim {
    public static final String VERSION = "2.0.0 beta";
    public static final String COPYRIGHT_YEAR = "2020";
    public static boolean GRAPHICAL_MODE = true;
    public static boolean PIPELINE_MODE = false;
    public static boolean LC3 = true;
    public static boolean P37X = false;

    public static boolean isGraphical() {
        return GRAPHICAL_MODE;
    }

    //TODO Implement pipelined system
    /**
     * Get if the simulated CPU is pipelined
     * @return if the CPU is pipelined
     */
    public static boolean isPipelined() {
        return PIPELINE_MODE;
    }

    /**
     * Get if the LC3 instruction set is being used
     *
     * @return whether or not LC3 is being used
     */
    public static boolean isLC3() {
        return LC3;
    }

    /**
     * Get if the P37X instruction set is being used
     *
     * @return whether or not P37X is being used
     */
    static boolean isP37X() {
        return P37X;
    }

    /**
     * Get the instruction set architecture being used
     *
     * @return a string for either LC3 or P37X
     */
    public static String getISA() {
        if (LC3) {
            return "LC3 ISA";
        } else {
            return P37X ? "P37X ISA" : null;
        }
    }

    /**
     * Get the current version of PennSim
     *
     * @return the version as a String
     */
    public static String getVersion() {
        return "Version " + VERSION;
    }

    public static void main(String[] args) {
        Logger.start();

        Startup startup = Startup.parseArgs(args);
        if (startup == null) {
            System.exit(0);
        } else {
            startup.run();
        }

//        String str = null;
//        System.out.println(getVersion() + "\n");
//
//        for (int i = 0; i < args.length; ++i) {
//            if (args[i].equalsIgnoreCase("-t")) {
//                GRAPHICAL_MODE = false;
//            } else if (args[i].equalsIgnoreCase("-s")) {
//                ++i;
//                if (i >= args.length) {
//                    System.out.println("Error: -s requires a script filename");
//                    return;
//                }
//
//                str = args[i];
//            } else if (args[i].equalsIgnoreCase("-lc3")) {
//                LC3 = true;
//            } else if (args[i].equalsIgnoreCase("-p37x")) {
//                P37X = true;
//            } else {
//                if (!args[i].equalsIgnoreCase("-pipeline")) {
//                    System.out.println("Arg '" + args[i] + "' not recognized");
//                    printUsage();
//                    return;
//                }
//
//                PIPELINE_MODE = true;
//            }
//        }
//
//        if (LC3 && P37X) {
//            System.err.println("Error: can't specify more than one ISA");
//            printUsage();
//        } else if (!LC3 && !P37X) {
//            System.err.println("Error: ISA not specified");
//            printUsage();
//        } else {
//            System.out.println(getISA());
//            Machine machine = new Machine();
//            CommandLine commandLine = new CommandLine(machine);
//            if (str != null) {
//                commandLine.scheduleCommand("@script " + str);
//            }
//
//            if (GRAPHICAL_MODE) {
//                System.out.println("Loading graphical interface\n");
//                GUI gui = new GUI(machine, commandLine);
//                machine.setGUI(gui);
//                SwingUtilities.invokeLater(new TempRun(gui));
//            } else {
//                try {
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//                    String command;
//
//                    while (true) {
//                        if (!machine.isContinueMode()) {
//                            System.out.print(CommandLine.PROMPT);
//                        }
//
//                        if (str == null) {
//                            String line = reader.readLine();
//                            if (line != null) {
//                                commandLine.scheduleCommand(line);
//                            }
//                        }
//
//                        while (commandLine.hasMoreCommands() && (!machine.isContinueMode() || commandLine.hasQueuedStop())) {
//                            String nextCommand = commandLine.getNextCommand();
//                            if (str != null && !nextCommand.startsWith("@")) {
//                                str = null;
//                            }
//
//                            try {
//                                command = commandLine.runCommand(nextCommand);
//                            } catch (GenericException e) {
//                                command = e.getExceptionDescription();
//                            } catch (NumberFormatException e) {
//                                command = "NumberFormatException: " + e.getMessage();
//                            }
//
//                            if (command == null) {
//                                machine.cleanup();
//                                System.out.println("Bye!");
//                                return;
//                            }
//
//                            System.out.println(command);
//                        }
//
//                        if (str != null && !commandLine.hasMoreCommands()) {
//                            str = null;
//                        }
//                    }
//                } catch (IOException e) {
//                    ErrorLog.logError(e);
//                }
//            }
//
//        }
    }
}
