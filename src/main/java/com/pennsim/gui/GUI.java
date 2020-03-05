package com.pennsim.gui;

import com.pennsim.CommandLine;
import com.pennsim.Machine;
import com.pennsim.Memory;
import com.pennsim.PennSim;
import com.pennsim.RegisterFile;
import com.pennsim.exception.GenericException;
import com.pennsim.util.ErrorLog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class GUI implements ActionListener, TableModelListener {

    static final Color BREAK_POINT_COLOR = new Color(241, 103, 103);
    private static final Color PC_COLOR;
    // Various UI themes (WARNING: Some do not work well)
    // TODO: Add option to change theming in the program
    private static final String LOOKANDFEEL = "Metal";
//   private static final String LOOKANDFEEL = "System";
//   private static final String LOOKANDFEEL = "Motif";
//   private static final String LOOKANDFEEL = "GTK+";

    static {
        PC_COLOR = Color.YELLOW;
    }

    private final JFileChooser fileChooser;
    private final JMenuBar menuBar;
    private final JMenu fileMenu;
    private final JMenu aboutMenu;
    private final JMenuItem openItem;
    private final JMenuItem quitItem;
    private final JMenuItem commandItem;
    private final JMenuItem versionItem;
    private final String openActionCommand;
    private final String quitActionCommand;
    private final String openCOWActionCommand;
    private final String versionActionCommand;
    private final JPanel controlPanel;
    private final JButton nextButton;
    private final String nextButtonCommand;
    private final JButton stepButton;
    private final String stepButtonCommand;
    private final JButton continueButton;
    private final String continueButtonCommand;
    private final JButton stopButton;
    private final String stopButtonCommand;
    private final JButton resetButton;
    private final String resetButtonCommand;
    private final String statusLabelRunning;
    private final String statusLabelSuspended;
    private final String statusLabelHalted;
    private final JLabel statusLabel;
    private final Color runningColor;
    private final Color suspendedColor;
    private final Color haltedColor;
    private final JTable regTable;
    private final CommandLinePanel commandPanel;
    private final CommandOutputWindow commandOutputWindow;
    private final JPanel memoryPanel;
    private final JTable memTable;
    private final JScrollPane memScrollPane;
    private final Machine machine;
    private final JFrame frame;
    private final JPanel devicePanel;
    private final JPanel registerPanel;
    private final TextConsolePanel ioPanel;
    private final VideoConsole video;

    public GUI(final Machine machine, CommandLine commandLine) {
        this.frame = new JFrame("PennSim - " + PennSim.VERSION + " - " + PennSim.getISA());
        this.fileChooser = new JFileChooser(".");
        this.menuBar = new JMenuBar();
        this.fileMenu = new JMenu("File");
        this.aboutMenu = new JMenu("About");
        this.openItem = new JMenuItem("Open .obj File");
        this.quitItem = new JMenuItem("Quit");
        this.commandItem = new JMenuItem("Open Command Output Window");
        this.versionItem = new JMenuItem("Simulator Version");
        this.openActionCommand = "Open";
        this.quitActionCommand = "Quit";
        this.openCOWActionCommand = "OutputWindow";
        this.versionActionCommand = "Version";
//        JPanel leftPanel = new JPanel();
        this.controlPanel = new JPanel();
        this.nextButton = new JButton("Next");
        this.nextButtonCommand = "Next";
        this.stepButton = new JButton("Step");
        this.stepButtonCommand = "Step";
        this.continueButton = new JButton("Continue");
        this.continueButtonCommand = "Continue";
        this.stopButton = new JButton("Stop");
        this.stopButtonCommand = "Stop";
        this.resetButton = new JButton("Reset");
        this.resetButtonCommand = "Reset";
        this.statusLabelRunning = "    Running ";
        this.statusLabelSuspended = "Suspended ";
        this.statusLabelHalted = "       Halted ";
        this.statusLabel = new JLabel("");
        this.runningColor = new Color(43, 129, 51);
        this.suspendedColor = new Color(209, 205, 93);
        this.haltedColor = new Color(161, 37, 40);
        this.memoryPanel = new JPanel(new BorderLayout());
        this.devicePanel = new JPanel();
        this.registerPanel = new JPanel();
        this.machine = machine;
        RegisterFile registerFile = machine.getRegisterFile();
        this.regTable = new JTable(registerFile);
        TableColumn column = this.regTable.getColumnModel().getColumn(0);
        column.setMaxWidth(30);
        column.setMinWidth(30);
        column = this.regTable.getColumnModel().getColumn(2);
        column.setMaxWidth(30);
        column.setMinWidth(30);
        Memory memory = machine.getMemory();
        this.memTable = new JTable(memory) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                if (column == 0) {
                    JCheckBox checkBox = new JCheckBox();
                    if (row < 65024) {
                        if (GUI.this.machine.getMemory().isBreakPointSet(row)) {
                            checkBox.setSelected(true);
                            checkBox.setBackground(GUI.BREAK_POINT_COLOR);
                            checkBox.setForeground(GUI.BREAK_POINT_COLOR);
                        } else {
                            checkBox.setSelected(false);
                            checkBox.setBackground(this.getBackground());
                        }
                    } else {
                        checkBox.setEnabled(false);
                        checkBox.setBackground(Color.lightGray);
                    }

                    return checkBox;
                } else {
                    if (row == GUI.this.machine.getRegisterFile().getPC()) {
                        component.setBackground(GUI.PC_COLOR);
                    } else if (GUI.this.machine.getMemory().isBreakPointSet(row)) {
                        component.setBackground(GUI.BREAK_POINT_COLOR);
                    } else {
                        component.setBackground(this.getBackground());
                    }

                    return component;
                }
            }

            public void tableChanged(TableModelEvent event) {
                if (machine != null) {
                    super.tableChanged(event);
                }

            }
        };
        this.memScrollPane = new JScrollPane(this.memTable) {
            public JScrollBar createVerticalScrollBar() {
                return new HighlightScrollBar(machine);
            }
        };
        this.memScrollPane.getVerticalScrollBar()
                .setBlockIncrement(this.memTable.getModel().getRowCount() / 512);
        this.memScrollPane.getVerticalScrollBar().setUnitIncrement(1);
        column = this.memTable.getColumnModel().getColumn(0);
        column.setMaxWidth(20);
        column.setMinWidth(20);
        column.setCellEditor(new DefaultCellEditor(new JCheckBox()));
        column = this.memTable.getColumnModel().getColumn(2);
        column.setMinWidth(50);
        column.setMaxWidth(50);
        this.commandPanel = new CommandLinePanel(machine, commandLine);
        this.commandOutputWindow = new CommandOutputWindow("Command Output");
        WindowListener listener = new WindowListener() {
            public void windowActivated(WindowEvent event) {
            }

            public void windowClosed(WindowEvent event) {
            }

            public void windowClosing(WindowEvent event) {
                GUI.this.commandOutputWindow.setVisible(false);
            }

            public void windowDeactivated(WindowEvent event) {
            }

            public void windowDeiconified(WindowEvent event) {
            }

            public void windowIconified(WindowEvent event) {
            }

            public void windowOpened(WindowEvent event) {
            }
        };
        this.commandOutputWindow.addWindowListener(listener);
        this.commandOutputWindow.setSize(700, 600);
        Console.registerConsole(this.commandPanel);
        Console.registerConsole(this.commandOutputWindow);
        this.ioPanel = new TextConsolePanel(machine.getMemory().getKeyBoardDevice(), machine.getMemory().getMonitor());
        this.ioPanel.setMinimumSize(new Dimension(256, 85));
        this.video = new VideoConsole(machine);
        this.commandPanel.setGUI(this);
    }

    /**
     * Initialize the UI theme
     */
    public static void initLookAndFeel() {
        String theme;
        JFrame.setDefaultLookAndFeelDecorated(true);
        if (LOOKANDFEEL != null) {
            switch (LOOKANDFEEL) {
                case "Metal":
                    theme = UIManager.getCrossPlatformLookAndFeelClassName();
                    break;
                case "System":
                    theme = UIManager.getSystemLookAndFeelClassName();
                    break;
                case "Motif":
                    theme = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
                    break;
                case "GTK+":
                    theme = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
                    break;
                default:
                    ErrorLog.logError("Unexpected value of LOOKANDFEEL specified: " + LOOKANDFEEL);
                    theme = UIManager.getCrossPlatformLookAndFeelClassName();
                    break;
            }

            try {
                UIManager.setLookAndFeel(theme);
            } catch (ClassNotFoundException e) {
                ErrorLog.logError("Couldn't find class for specified look and feel:" + theme);
                ErrorLog.logError("Did you include the L&F library in the class path?");
                ErrorLog.logError("Using the default look and feel.");
            } catch (UnsupportedLookAndFeelException e) {
                ErrorLog.logError("Can't use the specified look and feel (" + theme + ") on this platform.");
                ErrorLog.logError("Using the default look and feel.");
            } catch (Exception e) {
                ErrorLog.logError("Couldn't get specified look and feel (" + theme + "), for some reason.");
                ErrorLog.logError("Using the default look and feel.");
                ErrorLog.logError(e);
            }
        }

    }

    /**
     * Set up the Memory Panel
     */
    private void setupMemoryPanel() {
        this.memoryPanel.add(this.memScrollPane, "Center");
        this.memoryPanel.setMinimumSize(new Dimension(400, 100));
        this.memoryPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Memory"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.memTable.getModel().addTableModelListener(this);
        this.memTable.getModel().addTableModelListener(this.video);
        this.memTable.getModel().addTableModelListener(
                (HighlightScrollBar) this.memScrollPane.getVerticalScrollBar());
        this.memTable.setPreferredScrollableViewportSize(new Dimension(400, 460));
    }

    /**
     * Set up the Device Panel
     */
    private void setupDevicePanel() {
        this.devicePanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = 10;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.75D;
        this.devicePanel.add(this.video, constraints);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 0.25D;
        constraints.fill = 0;
        this.devicePanel.add(this.ioPanel, constraints);
        this.devicePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Devices"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.devicePanel.setVisible(true);
    }

    /**
     * Set up the Console Panel
     */
    private void setupControlPanel() {
        //TODO: Figure out why this boolean is here
//        boolean var1 = true;
        this.controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        // Next Button
        this.nextButton.setActionCommand(nextButtonCommand);
        this.nextButton.addActionListener(this);
        constraints.weightx = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.controlPanel.add(this.nextButton, constraints);

        // Step Button
        this.stepButton.setActionCommand(stepButtonCommand);
        this.stepButton.addActionListener(this);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.controlPanel.add(this.stepButton, constraints);

        // Continue Button
        this.continueButton.setActionCommand(continueButtonCommand);
        this.continueButton.addActionListener(this);
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.controlPanel.add(this.continueButton, constraints);

        // Stop Button
        this.stopButton.setActionCommand(stopButtonCommand);
        this.stopButton.addActionListener(this);
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.controlPanel.add(this.stopButton, constraints);

        // Reset Button
        // TODO Have a dialog pop up confirming the action
        this.resetButton.setActionCommand(resetButtonCommand);
        this.resetButton.addActionListener(this);
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.controlPanel.add(this.resetButton, constraints);

        // Status Label
        constraints.gridx = 5;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.LINE_END;
        this.setStatusLabelSuspended();
        this.controlPanel.add(this.statusLabel, constraints);

        // Space between buttons and CommandPanel
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 6;
//        this.controlPanel.add(Box.createRigidArea(new Dimension(5, 5)), constraints);
        this.controlPanel.add(Box.createRigidArea(new Dimension(8, 8)), constraints);

        // Command Panel
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 6;
        constraints.gridheight = 1;
        constraints.ipady = 100;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        this.controlPanel.add(this.commandPanel, constraints);

        this.controlPanel.setMinimumSize(new Dimension(100, 150));
        this.controlPanel.setPreferredSize(new Dimension(100, 150));
        this.controlPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Controls"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.controlPanel.setVisible(true);
    }

    /**
     * Set up the Register Panel
     */
    private void setupRegisterPanel() {
        this.registerPanel.setLayout(new GridBagLayout());
        GridBagConstraints grid = new GridBagConstraints();
        grid.gridx = 0;
        grid.gridy = 0;
        grid.weightx = 1.0;
        grid.fill = 2;
        this.registerPanel.add(this.regTable, grid);
        this.registerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Registers"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.registerPanel.setVisible(true);
    }

    /**
     * Set up the overall GUI
     */
    public void setUpGUI() {
        initLookAndFeel();
        JFrame.setDefaultLookAndFeelDecorated(true);
        this.machine.setStoppedListener(this.commandPanel);
        this.fileChooser.setFileSelectionMode(2);
        this.fileChooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    String name = file.getName();
                    return name != null && name.toLowerCase().endsWith(".obj");
                }
            }

            public String getDescription() {
                return "*.obj";
            }
        });
        this.openItem.setActionCommand("Open");
        this.openItem.addActionListener(this);
        this.fileMenu.add(this.openItem);
        this.commandItem.setActionCommand("OutputWindow");
        this.commandItem.addActionListener(this);
        this.fileMenu.add(this.commandItem);
        this.fileMenu.addSeparator();
        this.quitItem.setActionCommand("Quit");
        this.quitItem.addActionListener(this);
        this.fileMenu.add(this.quitItem);
        this.versionItem.setActionCommand("Version");
        this.versionItem.addActionListener(this);
        this.aboutMenu.add(this.versionItem);
        this.menuBar.add(this.fileMenu);
        this.menuBar.add(this.aboutMenu);
        this.frame.setJMenuBar(this.menuBar);
        this.setupControlPanel();
        this.setupDevicePanel();
        this.setupMemoryPanel();
        this.setupRegisterPanel();
        this.regTable.getModel().addTableModelListener(this);
        this.frame.getContentPane().setLayout(new GridBagLayout());

        JTabbedPane tabPane = new JTabbedPane();

        JComponent simTab = initSimTab();
        JComponent editorTab = initEditorTab();

        tabPane.addTab("Simulator", null, simTab, "Simulation Tab");
//        tabPane.addTab("Editor", null, editorTab, "Editor Tab");

        GridBagConstraints grid = new GridBagConstraints();
        grid.fill = GridBagConstraints.BOTH;
        grid.weightx = 1;
        grid.weighty = 1;
        this.frame.getContentPane().add(tabPane, grid);

        this.frame.setSize(new Dimension(700, 750));
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.pack();
        this.frame.setVisible(true);
        this.scrollToPC();
        this.commandPanel.actionPerformed(null);
    }

    JComponent initEditorTab() {
        JPanel panel = new JPanel(false);
        JLabel text = new JLabel("Editor Pane...");
        text.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(text);
        return panel;
    }

    JComponent initSimTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints grid = new GridBagConstraints();
        grid.fill = GridBagConstraints.BOTH;
        grid.gridx = 0;
        grid.gridy = 0;
        grid.gridwidth = 3;
        grid.gridheight = 1;
        grid.weightx = 1;
        grid.weighty = 0.5;
        panel.add(this.controlPanel, grid);

        grid = new GridBagConstraints();
        grid.fill = GridBagConstraints.BOTH;
        grid.gridx = 0;
        grid.gridy = 1;
        grid.gridwidth = 1;
        grid.gridheight = 1;
        panel.add(this.registerPanel, grid);

        grid = new GridBagConstraints();
        grid.fill = GridBagConstraints.BOTH;
        grid.gridx = 0;
        grid.gridy = 2;
        grid.gridwidth = 1;
        grid.gridheight = 2;
        panel.add(this.devicePanel, grid);

        grid = new GridBagConstraints();
        grid.fill = GridBagConstraints.BOTH;
        grid.gridx = 1;
        grid.gridy = 1;
        grid.gridwidth = 2;
        grid.gridheight = 3;
        grid.weighty = 1;
        panel.add(this.memoryPanel, grid);

        return panel;
    }

    /**
     * Scroll the Memory Panel to a specific row
     *
     * @param row the row to scroll to
     */
    public void scrollToIndex(int row) {
        this.memTable.scrollRectToVisible(this.memTable.getCellRect(row, 0, true));
    }

    /**
     * Scroll the Memory Panel to the row defined by the PC
     */
    public void scrollToPC() {
        this.scrollToPC(0);
    }

    public void scrollToPC(int row) {
        int address = this.machine.getRegisterFile().getPC() + row;
        this.memTable.scrollRectToVisible(this.memTable.getCellRect(address, 0, true));
    }

    @Deprecated
    public void tableChanged(TableModelEvent event) { }

    /**
     * Confirm exit when exiting the program
     */
    void confirmExit() {
        Object[] options = new Object[]{"Yes", "No"};
        int optionDialog = JOptionPane.showOptionDialog(this.frame, "Are you sure you want to quit?",
                "Quit verification", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (optionDialog == 0) {
            this.machine.cleanup();
            this.frame.setVisible(false);
            this.frame.dispose();
            System.exit(0);
        }
    }

    /**
     * Function to handle each event button in the GUI
     *
     * @param event the event to be handled
     */
    public void actionPerformed(ActionEvent event) {
        try {
            int index;
            try {
                index = Integer.parseInt(event.getActionCommand());
                this.scrollToIndex(index);
            } catch (NumberFormatException var4) {
                if (nextButtonCommand.equals(event.getActionCommand())) {
                    this.machine.executeNext();
                } else if (stopButtonCommand.equals(event.getActionCommand())) {
                    this.machine.executeStep();
                } else if (continueButtonCommand.equals(event.getActionCommand())) {
                    this.machine.executeMany();
                } else if (quitActionCommand.equals(event.getActionCommand())) {
                    this.confirmExit();
                } else if (stopButtonCommand.equals(event.getActionCommand())) {
                    Console.println(this.machine.stopExecution(true));
                } else if (resetButtonCommand.equals(event.getActionCommand())) {
                    this.machine.reset();
                    Console.println("System reset");
                } else if (openCOWActionCommand.equals(event.getActionCommand())) {
                    this.commandOutputWindow.setVisible(true);
                } else if (versionActionCommand.equals(event.getActionCommand())) {
                    JOptionPane.showMessageDialog(this.frame, PennSim.getVersion(),
                            "Version", JOptionPane.INFORMATION_MESSAGE);
                } else if (openActionCommand.equals(event.getActionCommand())) {
                    index = this.fileChooser.showOpenDialog(this.frame);
                    if (index == 0) {
                        File file = this.fileChooser.getSelectedFile();
                        Console.println(this.machine.loadObjectFile(file));
                    } else {
                        Console.println("Open command cancelled by user.");
                    }
                }
            }
        } catch (GenericException e) {
            e.showMessageDialog(this.frame);
        }

    }

    /**
     * Get this JFrame
     *
     * @return the current JFrame
     */
    public JFrame getFrame() {
        return this.frame;
    }

    /**
     * Set the status label as "Running"
     */
    public void setStatusLabelRunning() {
        this.statusLabel.setText(statusLabelRunning);
        this.statusLabel.setForeground(this.runningColor);
    }

    /**
     * Set the status label as "Suspended"
     */
    public void setStatusLabelSuspended() {
        this.statusLabel.setText(statusLabelSuspended);
        this.statusLabel.setForeground(this.suspendedColor);
    }

    /**
     * Set the status label as "Halted"
     */
    public void setStatusLabelHalted() {
        this.statusLabel.setText(statusLabelHalted);
        this.statusLabel.setForeground(this.haltedColor);
    }

    /**
     * Set the status label as either "Suspended" or
     * "Running" based on the boolean input
     */
    public void setStatusLabel(boolean isSuspended) {
        if (isSuspended) {
            this.setStatusLabelSuspended();
        } else {
            this.setStatusLabelHalted();
        }

    }

    public void setTextConsoleEnabled(boolean enabled) {
        this.ioPanel.setEnabled(enabled);
    }

    /**
     * Reset the GUI
     */
    public void reset() {
        this.setTextConsoleEnabled(true);
        this.commandPanel.reset();
        this.video.reset();
        this.scrollToPC();
    }
}
