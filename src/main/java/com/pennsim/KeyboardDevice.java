package com.pennsim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class KeyboardDevice {

    private static final Word KB_AVAILABLE = new Word(32768);
    private static final Word KB_UNAVAILABLE = new Word(0);
    public static int SCRIPT_MODE = 0;
    public static int INTERACTIVE_MODE = 1;
    private static int CBUFSIZE = 128;
    private static char TIMER_TICK = '.';
    private BufferedReader kbin = null;
    private BufferedReader defkbin = null;
    private int current = 0;
    private int mode;
    private int defmode;

    public KeyboardDevice() {
        this.kbin = new BufferedReader(new InputStreamReader(System.in));
        this.mode = INTERACTIVE_MODE;
        this.defkbin = this.kbin;
        this.defmode = this.mode;
    }

    public void setDefaultInputStream() {
        this.defkbin = this.kbin;
    }

    public void setDefaultInputMode() {
        this.defmode = this.mode;
    }

    public void setInputStream(InputStream var1) {
        this.kbin = new BufferedReader(new InputStreamReader(var1));
    }

    public void setInputMode(int var1) {
        this.mode = var1;
    }

    public void reset() {
        this.kbin = this.defkbin;
        this.mode = this.defmode;
        this.current = 0;
    }

    public Word status() {
        return this.available() ? KB_AVAILABLE : KB_UNAVAILABLE;
    }

    public boolean available() {
        try {
            if (this.kbin.ready()) {
                this.kbin.mark(1);
                if (this.kbin.read() == TIMER_TICK) {
                    this.kbin.reset();
                    return false;
                }

                this.kbin.reset();
                return true;
            }
        } catch (IOException var2) {
            ErrorLog.logError(var2);
        }

        return false;
    }

    public Word read() {
        char[] var1 = new char[CBUFSIZE];

        try {
            if (this.available()) {
                if (this.mode == INTERACTIVE_MODE) {
                    int var2 = this.kbin.read(var1, 0, CBUFSIZE);
                    this.current = var1[var2 - 1];
                } else {
                    this.current = this.kbin.read();
                }
            }
        } catch (IOException var3) {
            ErrorLog.logError(var3);
        }

        return new Word(this.current);
    }

    public boolean hasTimerTick() {
        try {
            this.kbin.mark(1);
            if (this.kbin.ready()) {
                if (this.kbin.read() == TIMER_TICK) {
                    return true;
                }

                this.kbin.reset();
                return false;
            }
        } catch (IOException var2) {
            ErrorLog.logError(var2);
        }

        return false;
    }
}
