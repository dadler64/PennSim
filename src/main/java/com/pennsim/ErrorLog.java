package com.pennsim;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

public class ErrorLog {

    private static PrintWriter log;
    private static String logDelim = "\n-----\n";
    private static boolean logOpen = false;

    private static void logTimeStamp() {
        if (!logOpen) {
            logInit();
        }

        if (log != null) {
            log.write((new Date(Calendar.getInstance().getTimeInMillis())).toString() + ": ");
        }
    }

    public static void logError(String var0) {
        if (!logOpen) {
            logInit();
        }

        if (log != null) {
            logTimeStamp();
            log.write(var0);
            log.write(logDelim);
        }
    }

    public static void logError(Exception var0) {
        if (!logOpen) {
            logInit();
        }

        if (log != null) {
            logTimeStamp();
            var0.printStackTrace(log);
            log.write(logDelim);
        }
    }

    private static void logInit() {
        if (!logOpen) {
            try {
                log = new PrintWriter(new FileWriter("pennsim_errorlog.txt"), true);
            } catch (IOException var1) {
                log = null;
            }
        }

    }

    public static void logClose() {
        if (log != null) {
            log.close();
        }
    }
}
