package com.pennsim;

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

public class CommandLinePanel extends JPanel implements ActionListener, PrintableConsole {

    private final CommandLine cmd;
    private final Machine mac;
    protected JTextField textField = new JTextField(20);
    protected JTextArea textArea;
    private GUI gui;

    public CommandLinePanel(Machine var1, CommandLine var2) {
        super(new GridBagLayout());
        this.textField.addActionListener(this);
        this.textField.getInputMap().put(KeyStroke.getKeyStroke("UP"), "prevHistory");
        this.textField.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "nextHistory");
        this.textField.getActionMap().put("prevHistory", new AbstractAction() {
            public void actionPerformed(ActionEvent var1) {
                CommandLinePanel.this.textField.setText(CommandLinePanel.this.cmd.getPrevHistory());
            }
        });
        this.textField.getActionMap().put("nextHistory", new AbstractAction() {
            public void actionPerformed(ActionEvent var1) {
                CommandLinePanel.this.textField.setText(CommandLinePanel.this.cmd.getNextHistory());
            }
        });
        this.mac = var1;
        this.cmd = var2;
        this.textArea = new JTextArea(5, 70);
        this.textArea.setEditable(false);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        JScrollPane var3 = new JScrollPane(this.textArea, 22, 30);
        GridBagConstraints var4 = new GridBagConstraints();
        var4.gridwidth = 0;
        var4.fill = 2;
        this.add(this.textField, var4);
        var4 = new GridBagConstraints();
        var4.gridwidth = 0;
        var4.fill = 1;
        var4.weightx = 1.0D;
        var4.weighty = 1.0D;
        this.add(var3, var4);
        this.setMinimumSize(new Dimension(20, 1));
    }

    public void setGUI(GUI var1) {
        this.cmd.setGUI(var1);
        this.gui = var1;
    }

    public void clear() {
        Document var1 = this.textArea.getDocument();

        try {
            var1.remove(0, var1.getLength());
        } catch (BadLocationException var3) {
            ErrorLog.logError(var3);
        }

    }

    public void actionPerformed(ActionEvent var1) {
        String var2;
        if (var1 != null) {
            var2 = this.textField.getText();
            this.cmd.scheduleCommand(var2);
        }

        while (this.cmd.hasMoreCommands() && (!this.mac.isContinueMode() || this.cmd
                .hasQueuedStop())) {
            try {
                var2 = this.cmd.runCommand(this.cmd.getNextCommand());
                if (var2 != null) {
                    if (var2.length() > 0) {
                        Console.println(var2);
                    }
                } else {
                    this.gui.confirmExit();
                }
            } catch (ExceptionException var3) {
                var3.showMessageDialog(this.getParent());
            }
        }

        this.textField.selectAll();
        this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
    }

    public void print(String var1) {
        this.textArea.append(var1);
    }

    public void reset() {
        this.cmd.reset();
    }
}
