package com.pennsim;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Console {

    public static final String NEWLINE = System.getProperty("line.separator");
    private static List consoles = new LinkedList();

    public Console() {
        throw new UnsupportedOperationException(
                "com.pennsim.Console is meant to be used statically.");
    }

    public static void registerConsole(PrintableConsole var0) {
        consoles.add(var0);
    }

    public static void println(String var0) {
        if (PennSim.isGraphical()) {
            Iterator var1 = consoles.iterator();

            while (var1.hasNext()) {
                ((PrintableConsole) var1.next()).print(var0 + NEWLINE);
            }
        } else {
            System.out.println(var0);
        }

    }

    public static void clear() {
        if (PennSim.isGraphical()) {
            Iterator var0 = consoles.iterator();

            while (var0.hasNext()) {
                ((PrintableConsole) var0.next()).clear();
            }
        }

    }
}
