package com.pennsim;

class TempRun implements Runnable {

    GUI ms;

    public TempRun(GUI var1) {
        this.ms = var1;
    }

    public void run() {
        this.ms.setUpGUI();
    }
}
