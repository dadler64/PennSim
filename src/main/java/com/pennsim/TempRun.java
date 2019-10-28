package com.pennsim;

import com.pennsim.gui.GUI;

/**
 * Class used to assist in initializing the GUI
 */
class TempRun implements Runnable {

    private GUI gui;

    TempRun(GUI gui) {
        this.gui = gui;
    }

    public void run() {
        this.gui.setUpGUI();
    }
}
