package com.pennsim.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

/**
 * Logger class designed to output both messages or exceptions to a log
 */
public class ErrorLog {

    private static PrintWriter log;
    private static String logDeliminator = "\n-----\n";
    private static boolean logOpen = false;

    /**
     * Long the time before the message
     */
    private static void logTimeStamp() {
        if (!logOpen) {
            logInit();
        }

        if (log != null) {
            log.write((new Date(Calendar.getInstance().getTimeInMillis())).toString() + ": ");
        }
    }

    /**
     * Log a message
     *
     * @param message the message to be logged
     */
    public static void logError(String message) {
        if (!logOpen) {
            logInit();
        }

        if (log != null) {
            logTimeStamp();
            log.write(message);
            log.write(logDeliminator);
        }
    }

    /**
     * Log an exception
     *
     * @param e the exception to be logged
     */
    public static void logError(Exception e) {
        if (!logOpen) {
            logInit();
        }

        if (log != null) {
            logTimeStamp();
            e.printStackTrace(log);
            log.write(logDeliminator);
        }
    }

    /**
     * Initialize the logger
     */
    private static void logInit() {
        if (!logOpen) {
            try {
                log = new PrintWriter(new FileWriter("pennsim_errorlog.txt"), true);
            } catch (IOException var1) {
                log = null;
            }
        }

    }

    /**
     * Close the logger
     */
    public static void logClose() {
        if (log != null) {
            log.close();
        }
    }
}
