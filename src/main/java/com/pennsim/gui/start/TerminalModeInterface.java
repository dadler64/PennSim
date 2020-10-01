package com.pennsim.gui.start;

import com.adlerd.logger.Logger;
import com.pennsim.Machine;
import com.pennsim.PennSim;
import com.pennsim.command.CommandLine;
import com.pennsim.exception.GenericException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TerminalModeInterface {

    private TerminalModeInterface() {
    }

    public static void run(Startup args) {
        String str = null;

        //TODO Fix scripts
//        if (args.equalsIgnoreCase("-s")) {
//            ++i;
//            if (i >= args.length) {
//                System.out.println("Error: -s requires a script filename");
//                return;
//            }
//            str = args[i];
//        }

        Logger.debugln(PennSim.getISA());

        Machine machine = new Machine();
        CommandLine commandLine = new CommandLine(machine);
        if (str != null) {
            commandLine.scheduleCommand("@script " + str);
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String command;

            while (true) {
                if (!machine.isContinueMode()) {
                    System.out.print(CommandLine.PROMPT);
                }

                if (str == null) {
                    String line = reader.readLine();
                    if (line != null) {
                        commandLine.scheduleCommand(line);
                    }
                }

                while (commandLine.hasMoreCommands() && (!machine.isContinueMode() || commandLine.hasQueuedStop())) {
                    String nextCommand = commandLine.getNextCommand();
                    if (str != null && !nextCommand.startsWith("@")) {
                        str = null;
                    }

                    try {
                        command = commandLine.runCommand(nextCommand);
                    } catch (GenericException e) {
                        command = e.getExceptionDescription();
                    } catch (NumberFormatException e) {
                        command = "NumberFormatException: " + e.getMessage();
                    }

                    if (command == null) {
                        machine.cleanup();
                        System.out.println("Bye!");
                        return;
                    }

                    System.out.println(command);
                }

                if (str != null && !commandLine.hasMoreCommands()) {
                    str = null;
                }
            }
        } catch (IOException e) {
            Logger.errorln(e);
        }
    }
}
