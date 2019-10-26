package com.pennsim;

import java.util.LinkedList;
import java.util.List;

/**
 * Class to represent the Console to print text to
 */
public class Console {

    private static final String NEWLINE = System.getProperty("line.separator");
    private static List<PrintableConsole> consoles = new LinkedList<>();

    public Console() {
        throw new UnsupportedOperationException(
                "Console is meant to be used statically.");
    }

    /**
     * Register the console
     *
     * @param printableConsole the console to register
     */
    static void registerConsole(PrintableConsole printableConsole) {
        consoles.add(printableConsole);
    }

    /**
     * Print some text to the console
     *
     * @param text the text to output
     */
    public static void println(String text) {
        if (PennSim.isGraphical()) {
            for (Object console : consoles) {
                ((PrintableConsole) console).print(text + NEWLINE);
            }
        } else {
            System.out.println(text);
        }

    }

    /**
     * Clear the console
     */
    public static void clear() {
        if (PennSim.isGraphical()) {
            for (Object console : consoles) {
                ((PrintableConsole) console).clear();
            }
        }

    }
}
