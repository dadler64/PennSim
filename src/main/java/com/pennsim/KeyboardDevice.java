package com.pennsim;

import com.pennsim.Word;
import com.pennsim.util.ErrorLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class KeyboardDevice {

    private static final Word KB_AVAILABLE = new Word(32768);
    private static final Word KB_UNAVAILABLE = new Word(0);
    public static int SCRIPT_MODE = 0;
    public static int INTERACTIVE_MODE = 1;
    private static char TIMER_TICK = '.';
    private BufferedReader keyBoardIn;
    private BufferedReader defaultKeyBoardIn;
    private int current = 0;
    private int mode;
    private int defaultMode;

    public KeyboardDevice() {
        this.keyBoardIn = new BufferedReader(new InputStreamReader(System.in));
        this.mode = INTERACTIVE_MODE;
        this.defaultKeyBoardIn = this.keyBoardIn;
        this.defaultMode = this.mode;
    }

    public void setDefaultInputStream() {
        this.defaultKeyBoardIn = this.keyBoardIn;
    }

    public void setDefaultInputMode() {
        this.defaultMode = this.mode;
    }

    public void setInputStream(InputStream var1) {
        this.keyBoardIn = new BufferedReader(new InputStreamReader(var1));
    }

    public void setInputMode(int var1) {
        this.mode = var1;
    }

    public void reset() {
        this.keyBoardIn = this.defaultKeyBoardIn;
        this.mode = this.defaultMode;
        this.current = 0;
    }

    public Word status() {
        return this.available() ? KB_AVAILABLE : KB_UNAVAILABLE;
    }

    public boolean available() {
        try {
            if (this.keyBoardIn.ready()) {
                this.keyBoardIn.mark(1);
                if (this.keyBoardIn.read() == TIMER_TICK) {
                    this.keyBoardIn.reset();
                    return false;
                }

                this.keyBoardIn.reset();
                return true;
            }
        } catch (IOException var2) {
            ErrorLog.logError(var2);
        }

        return false;
    }

    public Word read() {
        int CBUFSIZE = 128;
        char[] var1 = new char[CBUFSIZE];

        try {
            if (this.available()) {
                if (this.mode == INTERACTIVE_MODE) {
                    int var2 = this.keyBoardIn.read(var1, 0, CBUFSIZE);
                    this.current = var1[var2 - 1];
                } else {
                    this.current = this.keyBoardIn.read();
                }
            }
        } catch (IOException var3) {
            ErrorLog.logError(var3);
        }

        return new Word(this.current);
    }

    public boolean hasTimerTick() {
        try {
            this.keyBoardIn.mark(1);
            if (this.keyBoardIn.ready()) {
                if (this.keyBoardIn.read() == TIMER_TICK) {
                    return true;
                }

                this.keyBoardIn.reset();
                return false;
            }
        } catch (IOException var2) {
            ErrorLog.logError(var2);
        }

        return false;
    }
}
