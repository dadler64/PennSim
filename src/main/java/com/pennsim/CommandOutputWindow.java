package com.pennsim;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class CommandOutputWindow extends JFrame implements PrintableConsole {

    private JTextArea textArea = new JTextArea();

    CommandOutputWindow(String text) {
        super(text);
        this.textArea.setEditable(false);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(this.textArea, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.getContentPane().add(scrollPane);
    }

    /**
     * Print a message to the output window
     *
     * @param text the text to be printed
     */
    public void print(String text) {
        this.textArea.append(text);
    }

    /**
     * Clear the output window
     */
    public void clear() {
        Document document = this.textArea.getDocument();

        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException e) {
            ErrorLog.logError(e);
        }

    }
}
