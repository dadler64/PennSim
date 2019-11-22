package com.pennsim.gui;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

import com.pennsim.util.ErrorLog;
import com.pennsim.KeyboardDevice;
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
    private PipedInputStream keyboardIn;
    private PipedOutputStream keyboardOut;

    TextConsolePanel(KeyboardDevice keyboardDevice, MonitorDevice monitorDevice) {
        this.screen.setEditable(false);
        this.screen.addKeyListener(this);
        this.screen.addFocusListener(this);
        this.screen.setLineWrap(true);
        this.screen.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(this.screen, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.keyboardOut = new PipedOutputStream();

        try {
            this.keyboardIn = new PipedInputStream(this.keyboardOut);
        } catch (IOException e) {
            ErrorLog.logError(e);
        }

        keyboardDevice.setInputStream(this.keyboardIn);
        keyboardDevice.setDefaultInputStream();
        keyboardDevice.setInputMode(KeyboardDevice.INTERACTIVE_MODE);
        keyboardDevice.setDefaultInputMode();
        monitorDevice.addActionListener(this);
        this.add(scrollPane);
    }

    // NOTE: Is triggered by the "reset" command
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof Integer) {
            Document document = this.screen.getDocument();

            try {
                document.remove(0, document.getLength());
            } catch (BadLocationException e) {
                Console.println(e.getMessage());
            }
        } else {
            String stringSource = (String) event.getSource();
            this.screen.append(stringSource);
        }
    }

    public void keyReleased(KeyEvent event) {
    }

    public void keyPressed(KeyEvent event) {
    }

    public void keyTyped(KeyEvent event) {
        char keyChar = event.getKeyChar();

        try {
            this.keyboardOut.write(keyChar);
            this.keyboardOut.flush();
        } catch (IOException e) {
            ErrorLog.logError(e);
        }

    }

    public void focusGained(FocusEvent event) {
        this.screen.setBackground(Color.yellow);
    }

    public void focusLost(FocusEvent event) {
        this.screen.setBackground(Color.white);
    }

    public void setEnabled(boolean shouldEnable) {
        this.screen.setEnabled(shouldEnable);
        if (shouldEnable) {
            this.screen.setBackground(Color.white);
        } else {
            this.screen.setBackground(Color.gray);
        }

    }
}
