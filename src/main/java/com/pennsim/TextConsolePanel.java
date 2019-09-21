package com.pennsim;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class TextConsolePanel extends JPanel implements KeyListener, FocusListener, ActionListener {

    private JTextArea screen = new JTextArea(5, 21);
    private JScrollPane spane;
    private KeyboardDevice kbd;
    private MonitorDevice monitor;
    private PipedInputStream kbin;
    private PipedOutputStream kbout;

    TextConsolePanel(KeyboardDevice var1, MonitorDevice var2) {
        this.screen.setEditable(false);
        this.screen.addKeyListener(this);
        this.screen.addFocusListener(this);
        this.screen.setLineWrap(true);
        this.screen.setWrapStyleWord(true);
        this.spane = new JScrollPane(this.screen, 22, 30);
        this.kbd = var1;
        this.kbout = new PipedOutputStream();

        try {
            this.kbin = new PipedInputStream(this.kbout);
        } catch (IOException var4) {
            ErrorLog.logError(var4);
        }

        var1.setInputStream(this.kbin);
        var1.setDefaultInputStream();
        var1.setInputMode(KeyboardDevice.INTERACTIVE_MODE);
        var1.setDefaultInputMode();
        this.monitor = var2;
        var2.addActionListener(this);
        this.add(this.spane);
    }

    // NOTE: Is triggered by the "reset" command
    public void actionPerformed(ActionEvent var1) {
        Object var2 = var1.getSource();
        if (var2 instanceof Integer) {
            Document var3 = this.screen.getDocument();

            try {
                var3.remove(0, var3.getLength());
            } catch (BadLocationException var5) {
                Console.println(var5.getMessage());
            }
        } else {
            String var6 = (String) var1.getSource();
            this.screen.append(var6);
        }

    }

    public void keyReleased(KeyEvent var1) {
    }

    public void keyPressed(KeyEvent var1) {
    }

    public void keyTyped(KeyEvent var1) {
        char var2 = var1.getKeyChar();

        try {
            this.kbout.write(var2);
            this.kbout.flush();
        } catch (IOException var4) {
            ErrorLog.logError(var4);
        }

    }

    public void focusGained(FocusEvent var1) {
        this.screen.setBackground(Color.yellow);
    }

    public void focusLost(FocusEvent var1) {
        this.screen.setBackground(Color.white);
    }

    public void setEnabled(boolean var1) {
        this.screen.setEnabled(var1);
        if (var1) {
            this.screen.setBackground(Color.white);
        } else {
            this.screen.setBackground(Color.gray);
        }

    }
}
