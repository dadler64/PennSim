package com.pennsim.gui;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

import com.pennsim.CommandLine;
import com.pennsim.util.ErrorLog;
import com.pennsim.exception.GenericException;
import com.pennsim.Machine;
import com.pennsim.PrintableConsole;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Class which represents the command line panel where you for the program
 */
public class CommandLinePanel extends JPanel implements ActionListener, PrintableConsole {

    private final CommandLine commandLine;
    private final Machine machine;
    private JTextField textField = new JTextField(20);
    private JTextArea textArea;
    private GUI gui;

    CommandLinePanel(Machine machine, CommandLine commandLine) {
        super(new GridBagLayout());
        this.textField.addActionListener(this);
        this.textField.getInputMap().put(KeyStroke.getKeyStroke("UP"), "prevHistory");
        this.textField.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "nextHistory");
        this.textField.getActionMap().put("prevHistory", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                CommandLinePanel.this.textField.setText(CommandLinePanel.this.commandLine.getPrevHistory());
            }
        });
        this.textField.getActionMap().put("nextHistory", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                CommandLinePanel.this.textField.setText(CommandLinePanel.this.commandLine.getNextHistory());
            }
        });
        this.machine = machine;
        this.commandLine = commandLine;
        this.textArea = new JTextArea(10, 70); // 5 rows longer than the original
        this.textArea.setEditable(false);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(this.textArea, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = 0;
        constraints.fill = 2;
        this.add(this.textField, constraints);
        constraints = new GridBagConstraints();
        constraints.gridwidth = 0;
        constraints.fill = 1;
        constraints.weightx = 1.0D;
        constraints.weighty = 1.0D;
        this.add(scrollPane, constraints);
        this.setMinimumSize(new Dimension(20, 1));
    }

    /**
     * Set attach this to the main GUI
     *
     * @param gui gui to be bound to
     */
    void setGUI(GUI gui) {
        this.commandLine.setGUI(gui);
        this.gui = gui;
    }

    /**
     * Clear the command line
     */
    public void clear() {
        Document document = this.textArea.getDocument();

        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException e) {
            ErrorLog.logError(e);
        }

    }

    /**
     * Execute the action based in the command inputted into the command line
     *
     * @param event event to be performed
     */
    public void actionPerformed(ActionEvent event) {
        String text;
        if (event != null) {
            text = this.textField.getText();
            this.commandLine.scheduleCommand(text);
        }

        while (this.commandLine.hasMoreCommands() && (!this.machine.isContinueMode() || this.commandLine.hasQueuedStop())) {
            try {
                text = this.commandLine.runCommand(this.commandLine.getNextCommand());
                if (text != null) {
                    if (text.length() > 0) {
                        Console.println(text);
                    }
                } else {
                    this.gui.confirmExit();
                }
            } catch (GenericException e) {
                e.showMessageDialog(this.getParent());
            }
        }

        this.textField.selectAll();
        this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
    }

    /**
     * Print text to the command line
     *
     * @param text the text to be printed
     */
    public void print(String text) {
        this.textArea.append(text);
    }

    /**
     * Reset the command line
     */
    public void reset() {
        this.commandLine.reset();
    }
}
