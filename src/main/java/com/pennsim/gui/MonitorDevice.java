package com.pennsim.gui;

import com.pennsim.util.ErrorLog;
import com.pennsim.PennSim;
import com.pennsim.Word;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

/**
 * Class which controls the logic for the PennSim Monitor which
 * outputs characters from programs onto a simulated computer monitor
 */
public class MonitorDevice {

    private static final Word MONITOR_READY = new Word(32768);
    private static final Word MONITOR_NOT_READY = new Word(0);
    private OutputStreamWriter writer;
    private LinkedList<ActionListener> monitorList;

    public MonitorDevice() {
        if (!PennSim.GRAPHICAL_MODE) {
            this.writer = new OutputStreamWriter(System.out);
        } else {
            this.monitorList = new LinkedList<>();
        }

    }

    public MonitorDevice(OutputStream stream) {
        this.writer = new OutputStreamWriter(stream);
    }

    /**
     * Add an action listener to the monitor list
     *
     * @param listener the action listener to be added
     */
    void addActionListener(ActionListener listener) {
        this.monitorList.add(listener);
    }

    /**
     * Get the status of the monitor
     *
     * @return status of monitor
     */
    public Word getStatus() {
        return this.ready() ? MONITOR_READY : MONITOR_NOT_READY;
    }

    /**
     * Check if the monitor is ready for usage
     *
     * @return if the monitor is ready
     */
    private boolean ready() {
        if (PennSim.GRAPHICAL_MODE) {
            return true;
        } else {
            try {
                this.writer.flush();
                return true;
            } catch (IOException e) {
                ErrorLog.logError(e);
                return false;
            }
        }
    }

    /**
     * Reset the monitor
     */
    public void reset() {
        if (PennSim.GRAPHICAL_MODE) {
            for (ActionListener listener : this.monitorList) {
                listener.actionPerformed(new ActionEvent(1, 0, null));
            }
        }

    }

    /**
     * Write a character to the monitor
     *
     * @param character the character to write to the monitor
     */
    public void write(char character) {
        if (PennSim.GRAPHICAL_MODE) {
            for (ActionListener listener : this.monitorList) {
                listener.actionPerformed(new ActionEvent(character + "", 0, null));
            }
        } else {
            try {
                this.writer.write(character);
                this.writer.flush();
            } catch (IOException e) {
                ErrorLog.logError(e);
            }
        }

    }
}
