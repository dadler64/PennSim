package com.pennsim.gui;

import com.pennsim.CommandLine;
import com.pennsim.Machine;
import com.pennsim.Memory;
import com.pennsim.PennSim;
import com.pennsim.RegisterFile;
import com.pennsim.exception.GenericException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class GUI implements ActionListener, TableModelListener {

    static final Color BREAK_POINT_COLOR = new Color(241, 103, 103);
    private static final Color PC_COLOR = Color.YELLOW;

    private final JFileChooser fileChooser;
    private final JMenuBar menuBar;
    private final JMenu fileMenu;
    private final JMenu themeMenu;
    private final JMenu aboutMenu;
    private final JMenuItem openItem;
    private final JMenuItem quitItem;
    private final JMenuItem commandOutputWinItem;
    private final JMenuItem versionItem;
    private final String openActionCommand;
    private final String openCOWActionCommand;
    private final String quitActionCommand;
    private final JMenuItem lightItem;
    private final String lightActionCommand;
    private final JMenuItem darkItem;
    private final String darkActionCommand;
    private final JMenuItem metalItem;
    private final String metalActionCommand;
    private final JMenuItem systemItem;
    private final String systemActionCommand;
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
    private final JButton newFileButton;
    private final String newFileButtonCommand;
    private final JButton openFileButton;
    private final String openFileButtonCommand;
    private final JButton saveFileButton;
    private final String saveFileButtonCommand;
    private final String statusLabelRunning;
    private final String statusLabelSuspended;
    private final String statusLabelHalted;
    private final JLabel statusLabel;
    private final Color runningColor;
    private final Color suspendedColor;
    private final Color haltedColor;
    private final JTable registerTable;
    private final CommandLinePanel commandPanel;
    private final CommandOutputWindow commandOutputWindow;
    private final JPanel memoryPanel;
    private final JTable memoryTable;
    private final JScrollPane memoryScrollPane;
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
        this.themeMenu = new JMenu("Theme");
        this.aboutMenu = new JMenu("About");
        this.openItem = new JMenuItem("Open .obj File");
        this.openActionCommand = "Open";
        this.commandOutputWinItem = new JMenuItem("Output Window");
        this.openCOWActionCommand = "OutputWindow";
        this.quitItem = new JMenuItem("Quit");
        this.quitActionCommand = "Quit";
        this.lightItem = new JRadioButtonMenuItem("Light");
        this.lightActionCommand = "light";
        this.darkItem = new JRadioButtonMenuItem("Dark");
        this.darkActionCommand = "Dark";
        this.metalItem = new JRadioButtonMenuItem("Metal");
        this.metalActionCommand = "Motif";
        this.systemItem = new JRadioButtonMenuItem("System");
        this.systemActionCommand = "System";
        this.versionItem = new JMenuItem("Simulator Version");
        this.versionActionCommand = "Version";
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
        this.newFileButton = new JButton("New");
        this.newFileButtonCommand = "New";
        this.openFileButton = new JButton("Open");
        this.openFileButtonCommand = "Open";
        this.saveFileButton = new JButton("Save");
        this.saveFileButtonCommand = "Save";
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

        // Register pane init
        RegisterFile registerFile = machine.getRegisterFile();
        this.registerTable = new JTable(registerFile);
        TableColumn column = this.registerTable.getColumnModel().getColumn(0);
        column.setMaxWidth(35);
        column.setMinWidth(35);
        column = this.registerTable.getColumnModel().getColumn(2);
        column.setMaxWidth(35);
        column.setMinWidth(35);
        Memory memory = machine.getMemory();
        this.memoryTable = new JTable(memory) {
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
        this.memoryScrollPane = new JScrollPane(this.memoryTable) {
            public JScrollBar createVerticalScrollBar() {
                return new HighlightScrollBar(machine);
            }
        };
        this.memoryScrollPane.getVerticalScrollBar()
                .setBlockIncrement(this.memoryTable.getModel().getRowCount() / 512);
        this.memoryScrollPane.getVerticalScrollBar().setUnitIncrement(1);
        column = this.memoryTable.getColumnModel().getColumn(0);
        column.setMaxWidth(25);
        column.setMinWidth(25);
        column.setCellEditor(new DefaultCellEditor(new JCheckBox()));
        column = this.memoryTable.getColumnModel().getColumn(2);
        column.setMinWidth(60);
        column.setMaxWidth(60);
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
     * Set the UI theme
     * @param lookAndFeel the desired theme you wish to use
     */
    public void setLookAndFeel(String lookAndFeel) {
        String theme;
        JFrame.setDefaultLookAndFeelDecorated(true);
        if (lookAndFeel != null) {
            switch (lookAndFeel) {
                case "Light":
                    if (!lightItem.isSelected()) lightItem.setSelected(true);
                    theme = "com.jtattoo.plaf.fast.FastLookAndFeel";
                    break;
                case "Dark":
                    if (!darkItem.isSelected()) darkItem.setSelected(true);
                    theme = "com.jtattoo.plaf.hifi.HiFiLookAndFeel"; // Dark Grey Theme
//                    theme = "com.jtattoo.plaf.noire.NoireLookAndFeel"; // Dark Theme
                    break;
                case "Metal":
                    if (!metalItem.isSelected()) metalItem.setSelected(true);
                    theme = UIManager.getCrossPlatformLookAndFeelClassName();
                    break;
                case "System":
                    if (!systemItem.isSelected()) systemItem.setSelected(true);
                    theme = UIManager.getSystemLookAndFeelClassName();
                    break;
                default: {
                    System.err.println("Unexpected value of lookAndFeel specified: " + lookAndFeel);
                    theme = UIManager.getCrossPlatformLookAndFeelClassName();
                    break;
                }
            }

            try {
                UIManager.setLookAndFeel(theme);
                SwingUtilities.updateComponentTreeUI(frame);
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't find class for specified look and feel:" + theme);
                System.err.println("Did you include the L&F library in the class path?");
                System.err.println("Using the default look and feel.");
            } catch (UnsupportedLookAndFeelException e) {
                System.err.println("Can't use the specified look and feel (" + theme + ") on this platform.");
                System.err.println("Using the default look and feel.");
            } catch (Exception e) {
                System.err.println("Couldn't get specified look and feel (" + theme + "), for some reason.");
                System.err.println("Using the default look and feel.");
                System.err.println(e.getMessage());
            }
        }

    }

    /**
     * Set up the Memory Panel
     */
    private void setupMemoryPanel() {
        this.memoryPanel.add(this.memoryScrollPane, "Center");
        this.memoryPanel.setMinimumSize(new Dimension(400, 100));
        this.memoryPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Memory"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.memoryTable.getModel().addTableModelListener(this);
        this.memoryTable.getModel().addTableModelListener(this.video);
        this.memoryTable.getModel().addTableModelListener((HighlightScrollBar) this.memoryScrollPane.getVerticalScrollBar());
        this.memoryTable.setPreferredScrollableViewportSize(new Dimension(400, 460));
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

        // Set up border
        this.controlPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Controls"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.controlPanel.setVisible(true);
    }

    /**
     * Set up the Register Panel
     */
    private void setupRegisterPanel() {
        this.registerPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.registerPanel.add(this.registerTable, constraints);

        // Set up border
        this.registerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Registers"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.registerPanel.setVisible(true);
    }

    /**
     * Set up the overall GUI
     */
    public void setUpGUI() {
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
        this.commandOutputWinItem.setActionCommand("OutputWindow");
        this.commandOutputWinItem.addActionListener(this);
        this.fileMenu.add(this.commandOutputWinItem);
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
        this.registerTable.getModel().addTableModelListener(this);
        this.frame.getContentPane().setLayout(new GridBagLayout());

        JTabbedPane tabPane = new JTabbedPane();

        JComponent simTab = setUpSimTab();
        JComponent editorTab = setUpEditorTab();

        tabPane.addTab("Editor", null, editorTab, "Editor Tab");
        tabPane.addTab("Simulator", null, simTab, "Simulation Tab");

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        this.frame.getContentPane().add(tabPane, constraints);
        this.frame.setJMenuBar(this.menuBar);

        setLookAndFeel("Metal");
        this.frame.setSize(new Dimension(700, 750));
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.pack();
        this.frame.setVisible(true);
        this.scrollToPC();
        this.commandPanel.actionPerformed(null);
    }

    // TODO look into turning this into tabbed files???
    JComponent setUpEditorTab() {
        JPanel panel = new JPanel(false);
        panel.setLayout(new GridBagLayout());

        // Set up button bar
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 0;

        JToolBar toolBar = setUpToolBar();
        panel.add(toolBar, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;

        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        panel.add(scrollPane, constraints);

        // TODO develop syntax highlighting
        // Set syntax highlighting
//        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
//        atmf.putMapping("text/lc3", "com.pennsim.util.LC3Syntax");
//        textArea.setSyntaxEditingStyle("text/lc3");

        return panel;
    }

    private JToolBar setUpToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setMargin(new Insets(4, 4, 4, 4));
        toolBar.setMinimumSize(new Dimension(800, 24));

        newFileButton.setActionCommand(newFileButtonCommand);
        newFileButton.addActionListener(this);
        toolBar.add(newFileButton);

        openFileButton.setActionCommand(openFileButtonCommand);
        openFileButton.addActionListener(this);
        toolBar.add(openFileButton);

        saveFileButton.setActionCommand(saveFileButtonCommand);
        saveFileButton.addActionListener(this);
        toolBar.add(saveFileButton);

        return toolBar;
    }

    private JComponent setUpSimTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        this.setUpMenuBar();
        this.setupControlPanel();
        this.setupDevicePanel();
        this.setupMemoryPanel();
        this.setupRegisterPanel();
        this.registerTable.getModel().addTableModelListener(this);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 0.5;
        panel.add(this.controlPanel, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        panel.add(this.registerPanel, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 2;
        panel.add(this.devicePanel, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 3;
        constraints.weighty = 1;
        panel.add(this.memoryPanel, constraints);

        return panel;
    }


    private void setUpMenuBar() {
        // File Menu
        this.openItem.setActionCommand(openActionCommand);
        this.openItem.addActionListener(this);
        this.fileMenu.add(this.openItem);
        this.commandOutputWinItem.setActionCommand(openCOWActionCommand);
        this.commandOutputWinItem.addActionListener(this);
        this.fileMenu.addSeparator();
        this.quitItem.setActionCommand(quitActionCommand);
        this.quitItem.addActionListener(this);
        this.fileMenu.add(this.quitItem);

        // Theme Menu
        this.lightItem.setActionCommand(lightActionCommand);
        this.lightItem.addActionListener(this);
        this.darkItem.setActionCommand(darkActionCommand);
        this.darkItem.addActionListener(this);
        this.metalItem.setActionCommand(metalActionCommand);
        this.metalItem.addActionListener(this);
        this.systemItem.setActionCommand(metalActionCommand);
        this.systemItem.addActionListener(this);

        ButtonGroup group = new ButtonGroup();
        group.add(this.lightItem);
        group.add(this.darkItem);
        group.add(this.metalItem);
        group.add(this.systemItem);

        this.themeMenu.add(lightItem);
        this.themeMenu.add(darkItem);
        this.themeMenu.add(metalItem);
        this.themeMenu.add(systemItem);

        // Version Menu
        this.versionItem.setActionCommand("Version");
        this.versionItem.addActionListener(this);
        this.aboutMenu.add(this.versionItem);

        //Menu Bar
        this.menuBar.add(this.fileMenu);
        this.menuBar.add(this.themeMenu);
        this.menuBar.add(this.aboutMenu);
    }

    /**
     * Scroll the Memory Panel to a specific row
     *
     * @param row the row to scroll to
     */
    public void scrollToIndex(int row) {
        this.memoryTable.scrollRectToVisible(this.memoryTable.getCellRect(row, 0, true));
    }

    /**
     * Scroll the Memory Panel to the row defined by the PC
     */
    public void scrollToPC() {
        this.scrollToPC(0);
    }

    public void scrollToPC(int row) {
        int address = this.machine.getRegisterFile().getPC() + row;
        this.memoryTable.scrollRectToVisible(this.memoryTable.getCellRect(address, 0, true));
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
            } catch (NumberFormatException nfe) {
                // TODO Create switch case statements for these
                // Control Buttons
                if (event.getSource() == nextButton) {
                    this.machine.executeNext();
                } else if (event.getSource() == stepButton) {
                    this.machine.executeStep();
                } else if (event.getSource() == continueButton) {
                    this.machine.executeMany();
                }else if (event.getSource() == stopButton) {
                    Console.println(this.machine.stopExecution(true));
                } else if (event.getSource() == resetButton) {
                    resetDialog();
                }
                // Editor buttons
                else if (event.getSource() == newFileButton) {
                    System.out.println("New File");
                } else if (event.getSource() == openFileButton) {
                    System.out.println("Open File");
                } else if (event.getSource() == saveFileButton) {
                    System.out.println("Save File");
                }

                // MenuBar items
                else if (openActionCommand.equals(event.getActionCommand())) {
                    index = this.fileChooser.showOpenDialog(this.frame);
                    if (index == 0) {
                        File file = this.fileChooser.getSelectedFile();
                        Console.println(this.machine.loadObjectFile(file));
                    } else {
                        Console.println("Open command cancelled by user.");
                    }
                } else if (openCOWActionCommand.equals(event.getActionCommand())) {
                    this.commandOutputWindow.setVisible(true);
                }else if (quitActionCommand.equals(event.getActionCommand())) {
                    this.confirmExit();
                } else if (lightActionCommand.equals(event.getActionCommand())) {
                    setLookAndFeel("Light");
                } else if (darkActionCommand.equals(event.getActionCommand())) {
                    setLookAndFeel("Dark");
                } else if (metalActionCommand.equals(event.getActionCommand())) {
                    setLookAndFeel("Metal");
                } else if (systemActionCommand.equals(event.getActionCommand())) {
                    setLookAndFeel("System");
                } else if (versionActionCommand.equals(event.getActionCommand())) {
                    JOptionPane.showMessageDialog(this.frame, PennSim.getVersion(),
                            "Version", JOptionPane.INFORMATION_MESSAGE);
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
