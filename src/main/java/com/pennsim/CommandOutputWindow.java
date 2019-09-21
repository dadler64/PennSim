package com.pennsim;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class CommandOutputWindow extends JFrame implements PrintableConsole {

    private JTextArea textArea = new JTextArea();

    public CommandOutputWindow(String var1) {
        super(var1);
        this.textArea.setEditable(false);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        JScrollPane var2 = new JScrollPane(this.textArea, 22, 30);
        this.getContentPane().add(var2);
    }

    public void print(String var1) {
        this.textArea.append(var1);
    }

    public void clear() {
        Document var1 = this.textArea.getDocument();

        try {
            var1.remove(0, var1.getLength());
        } catch (BadLocationException var3) {
            ErrorLog.logError(var3);
        }

    }
}
