package com.pennsim.command;


import com.pennsim.exception.GenericException;

public interface Command {

    /**
     * Define the usage of the command
     * @return The string being outputted to the console
     */
    String getUsage();

    /**
     * Help text of the command
     * @return The string being outputted to the console
     */
    String getHelp();


    String doCommand(String[] argArray, int argSize) throws GenericException;
}
