package com.pennsim;

public class TimerDevice {

    private static final Word TIMER_SET = new Word(32768);
    private static final Word TIMER_UNSET = new Word(0);
    private static final int MANUAL_TIMER = 0;
    private static final int AUTOMATIC_TIMER = 1;
    private static final long TIMER_INTERVAL = 500L;
    private int mode;
    private boolean enabled;
    private long lastTime;
    private long interval;
    private KeyboardDevice keyboardDevice = null;

    TimerDevice() {
        this.mode = AUTOMATIC_TIMER;
        this.enabled = true;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    void setEnabled(boolean shouldSet) {
        this.enabled = shouldSet;
    }

    long getInterval() {
        return this.interval;
    }

    void setTimer() {
        this.mode = AUTOMATIC_TIMER;
        this.interval = TIMER_INTERVAL;
        this.lastTime = System.currentTimeMillis();
    }

    void setTimer(long interval) {
        this.mode = AUTOMATIC_TIMER;
        this.interval = interval;
        this.lastTime = System.currentTimeMillis();
    }

    void setTimer(KeyboardDevice keyboardDevice) {
        this.mode = MANUAL_TIMER;
        this.interval = 1L;
        this.keyboardDevice = keyboardDevice;
    }

    public void reset() {
        this.mode = AUTOMATIC_TIMER;
        this.setTimer(TIMER_INTERVAL);
    }

    Word status() {
        return this.hasGoneOff() ? TIMER_SET : TIMER_UNSET;
    }

    private boolean hasGoneOff() {
        if (!this.enabled) {
            return false;
        } else if (this.mode == AUTOMATIC_TIMER) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastTime > this.interval) {
                this.lastTime = currentTime;
                return true;
            } else {
                return false;
            }
        } else {
            return this.keyboardDevice.hasTimerTick();
        }
    }
}
