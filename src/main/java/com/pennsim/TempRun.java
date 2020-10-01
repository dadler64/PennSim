package com.pennsim;

import com.pennsim.gui.GUI;

/**
 * Class used to assist in initializing the GUI
 */
public class TempRun implements Runnable {

    private final GUI gui;

    public TempRun(GUI gui) {
        this.gui = gui;
    }

    public void run() {
        this.gui.setUpGUI();
    }
}
