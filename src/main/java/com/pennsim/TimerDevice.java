package com.pennsim;

public class TimerDevice {

    private static final Word TIMER_SET = new Word(32768);
    private static final Word TIMER_UNSET = new Word(0);
    private static int MANUAL_TIMER = 0;
    private static int AUTOMATIC_TIMER = 1;
    private static long TIMER_INTERVAL = 500L;
    private int mode;
    private boolean enabled = false;
    private long lastTime;
    private long interval;
    private KeyboardDevice kb = null;

    public TimerDevice() {
        this.mode = AUTOMATIC_TIMER;
        this.enabled = true;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean var1) {
        this.enabled = var1;
    }

    public long getInterval() {
        return this.interval;
    }

    public void setTimer() {
        this.mode = AUTOMATIC_TIMER;
        this.interval = TIMER_INTERVAL;
        this.lastTime = System.currentTimeMillis();
    }

    public void setTimer(long var1) {
        this.mode = AUTOMATIC_TIMER;
        this.interval = var1;
        this.lastTime = System.currentTimeMillis();
    }

    public void setTimer(KeyboardDevice var1) {
        this.mode = MANUAL_TIMER;
        this.interval = 1L;
        this.kb = var1;
    }

    public void reset() {
        this.mode = AUTOMATIC_TIMER;
        this.setTimer(TIMER_INTERVAL);
    }

    public Word status() {
        return this.hasGoneOff() ? TIMER_SET : TIMER_UNSET;
    }

    public boolean hasGoneOff() {
        if (!this.enabled) {
            return false;
        } else if (this.mode == AUTOMATIC_TIMER) {
            long var1 = System.currentTimeMillis();
            if (var1 - this.lastTime > this.interval) {
                this.lastTime = var1;
                return true;
            } else {
                return false;
            }
        } else {
            return this.kb.hasTimerTick();
        }
    }
}
