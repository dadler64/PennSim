package com.pennsim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.ListIterator;

public class MonitorDevice {

    private static final Word MONITOR_READY = new Word(32768);
    private static final Word MONITOR_NOTREADY = new Word(0);
    private OutputStreamWriter dout;
    private LinkedList mlist;

    public MonitorDevice() {
        if (!PennSim.GRAPHICAL_MODE) {
            this.dout = new OutputStreamWriter(System.out);
        } else {
            this.mlist = new LinkedList();
        }

    }

    public MonitorDevice(OutputStream var1) {
        this.dout = new OutputStreamWriter(var1);
    }

    public void addActionListener(ActionListener var1) {
        this.mlist.add(var1);
    }

    public Word status() {
        return this.ready() ? MONITOR_READY : MONITOR_NOTREADY;
    }

    public boolean ready() {
        if (PennSim.GRAPHICAL_MODE) {
            return true;
        } else {
            try {
                this.dout.flush();
                return true;
            } catch (IOException var2) {
                ErrorLog.logError(var2);
                return false;
            }
        }
    }

    public void reset() {
        if (PennSim.GRAPHICAL_MODE) {
            ListIterator var1 = this.mlist.listIterator();

            while (var1.hasNext()) {
                ActionListener var2 = (ActionListener) var1.next();
                var2.actionPerformed(new ActionEvent(new Integer(1), 0, null));
            }
        }

    }

    public void write(char var1) {
        if (PennSim.GRAPHICAL_MODE) {
            ListIterator var2 = this.mlist.listIterator();

            while (var2.hasNext()) {
                ActionListener var3 = (ActionListener) var2.next();
                var3.actionPerformed(new ActionEvent(var1 + "", 0, null));
            }
        } else {
            try {
                this.dout.write(var1);
                this.dout.flush();
            } catch (IOException var4) {
                ErrorLog.logError(var4);
            }
        }

    }
}
