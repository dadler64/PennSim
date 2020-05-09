package com.pennsim.gui;

import com.pennsim.command.CommandLine;
import com.pennsim.Machine;
import com.pennsim.Memory;
import com.pennsim.PennSim;
import com.pennsim.RegisterFile;
import com.pennsim.exception.GenericException;
import com.pennsim.exception.PennSimException;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class GUI implements TableModelListener {

    static final Color BREAK_POINT_COLOR = new Color(241, 103, 103);
    private static final Color PC_COLOR = Color.YELLOW;

    private final JFileChooser fileChooser;
    private final JMenuBar menuBar;
    private final JMenu fileMenu;
    private final JMenu themeMenu;
    private final JMenu optionsMenu;
    private final JMenu aboutMenu;
    private final JMenuItem openFileItem;
    private final JMenuItem saveFileItem;
    private final JMenuItem newFileItem;
    private final JMenuItem quitItem;
    private final JMenuItem commandOutputWinItem;
    private final JMenuItem lightItem;
    private final JMenuItem darkItem;
    private final JMenuItem metalItem;
    private final JMenuItem systemItem;
    private final JMenuItem scrollLayoutItem;
    private final JMenuItem versionItem;
    private final JPanel controlPanel;
    private final JButton nextButton;
    private final JButton stepButton;
    private final JButton continueButton;
    private final JButton stopButton;
    private final JButton resetButton;
    private final JButton newFileButton;
    private final JButton openFileButton;
    private final JButton saveFileButton;
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
    private final EditorPanel editorPanel;
    private final LeftTabbedPanel leftTabbedPanel;
    private final JPanel mainPanel;
    private final JPanel terminalPanel;

    public GUI(final Machine machine, CommandLine commandLine) {
        this.frame = new JFrame("PennSim - " + PennSim.VERSION + " - " + PennSim.getISA());
        this.fileChooser = new JFileChooser(".");
        this.menuBar = new JMenuBar();
        this.fileMenu = new JMenu("File");
        this.themeMenu = new JMenu("Theme");
        this.optionsMenu = new JMenu("Options");
        this.aboutMenu = new JMenu("About");
        this.newFileItem = new JMenuItem("New File");
        this.openFileItem = new JMenuItem("Open File");
        this.saveFileItem = new JMenuItem("Save File");
        this.commandOutputWinItem = new JMenuItem("Output Window");
        this.quitItem = new JMenuItem("Quit");
        this.lightItem = new JRadioButtonMenuItem("Light");
        this.darkItem = new JRadioButtonMenuItem("Dark");
        this.metalItem = new JRadioButtonMenuItem("Metal");
        this.systemItem = new JRadioButtonMenuItem("System");
        this.scrollLayoutItem = new JCheckBoxMenuItem("Set ScrollLayout");
        this.versionItem = new JMenuItem("Simulator Version");
        this.controlPanel = new JPanel();
        this.nextButton = new JButton("Next");
        this.stepButton = new JButton("Step");
        this.continueButton = new JButton("Continue");
        this.stopButton = new JButton("Stop");
        this.resetButton = new JButton("Reset");
        this.newFileButton = new JButton("New");
        this.openFileButton = new JButton("Open");
        this.saveFileButton = new JButton("Save");
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
        this.terminalPanel = new JPanel();
        this.leftTabbedPanel = new LeftTabbedPanel();
        this.machine = machine;

        // Register pane init
        RegisterFile registerFile = machine.getRegisterFile();
        registerTable = new JTable(registerFile);
        TableColumn column = registerTable.getColumnModel().getColumn(0);
        column.setMaxWidth(35);
        column.setMinWidth(35);
        column = registerTable.getColumnModel().getColumn(2);
        column.setMaxWidth(35);
        column.setMinWidth(35);
        Memory memory = machine.getMemory();
        memoryTable = new JTable(memory) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                if (column == 0) {
                    JCheckBox checkBox = new JCheckBox();
                    if (row < 65024) {
                        if (machine.getMemory().isBreakPointSet(row)) {
                            checkBox.setSelected(true);
                            checkBox.setBackground(GUI.BREAK_POINT_COLOR);
                            checkBox.setForeground(GUI.BREAK_POINT_COLOR);
                        } else {
                            checkBox.setSelected(false);
                            checkBox.setBackground(getBackground());
                        }
                    } else {
                        checkBox.setEnabled(false);
                        checkBox.setBackground(Color.lightGray);
                    }

                    return checkBox;
                } else {
                    if (row == machine.getRegisterFile().getPC()) {
                        component.setBackground(GUI.PC_COLOR);
                    } else if (machine.getMemory().isBreakPointSet(row)) {
                        component.setBackground(GUI.BREAK_POINT_COLOR);
                    } else {
                        component.setBackground(getBackground());
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
        memoryScrollPane = new JScrollPane(memoryTable) {
            public JScrollBar createVerticalScrollBar() {
                return new HighlightScrollBar(machine);
            }
        };
        memoryScrollPane.getVerticalScrollBar().setBlockIncrement(memoryTable.getModel().getRowCount() / 512);
        memoryScrollPane.getVerticalScrollBar().setUnitIncrement(30); // Scroll Speed
        memoryScrollPane.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);
        memoryScrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        column = memoryTable.getColumnModel().getColumn(0);
        column.setMaxWidth(25);
        column.setMinWidth(25);
        column.setCellEditor(new DefaultCellEditor(new JCheckBox()));
        column = memoryTable.getColumnModel().getColumn(2);
        column.setMinWidth(60);
        column.setMaxWidth(60);
        commandPanel = new CommandLinePanel(machine, commandLine);
        commandOutputWindow = new CommandOutputWindow("Command Output");
        WindowListener listener = new WindowListener() {
            public void windowActivated(WindowEvent event) {

            }

            public void windowClosed(WindowEvent event) {
            }

            public void windowClosing(WindowEvent event) {
                commandOutputWindow.setVisible(false);
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
        commandOutputWindow.addWindowListener(listener);
        commandOutputWindow.setSize(700, 600);
        Console.registerConsole(commandPanel);
        Console.registerConsole(commandOutputWindow);
        ioPanel = new TextConsolePanel(machine.getMemory().getKeyBoardDevice(), machine.getMemory().getMonitor());
        ioPanel.setMinimumSize(new Dimension(256, 85));
        video = new VideoConsole(machine);
        commandPanel.setGUI(this);
        editorPanel = new EditorPanel();
        mainPanel = new JPanel();
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
     * Set up the overall GUI
     */
    public void setUpGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);

        setUpMenuBar();
        setupControlPanel();
        setupRegisterPanel();
        setupDevicePanel();
        setUpEditorPanel();
        setupMemoryPanel();
        setupTerminalPanel();

        setUpMainPanel();

        machine.setStoppedListener(commandPanel);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    String name = file.getName();
                    return name.toLowerCase().endsWith(".obj");
                }
            }

            public String getDescription() {
                return "Object Files (*.obj)";
            }
        });
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    String name = file.getName();
                    return name.toLowerCase().endsWith(".asm");
                }
            }

            @Override
            public String getDescription() {
                return "Assembly Files (*.asm)";
            }
        });
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    String name = file.getName();
                    return name.toLowerCase().endsWith(".txt");
                }
            }

            @Override
            public String getDescription() {
                return "Text Files (*.txt)";
            }
        });
        frame.setJMenuBar(menuBar);
        registerTable.getModel().addTableModelListener(this);
        frame.getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        frame.getContentPane().add(mainPanel, constraints);

        setLookAndFeel("Metal");
        frame.setPreferredSize(new Dimension(1050, 750));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        scrollToPC();
        commandPanel.actionPerformed(null);
    }

    private void setUpMainPanel() {
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(controlPanel, BorderLayout.PAGE_START);
        mainPanel.add(registerPanel, BorderLayout.LINE_START);
        mainPanel.add(devicePanel, BorderLayout.LINE_START);
        mainPanel.add(editorPanel, BorderLayout.CENTER);
        mainPanel.add(memoryPanel, BorderLayout.LINE_END);
        mainPanel.add(terminalPanel, BorderLayout.PAGE_END);
    }

    private void setUpMenuBar() {
        // File Menu
        newFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        openFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        saveFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.META_DOWN_MASK));

        newFileItem.addActionListener(e -> editorPanel.addFileTab());
        openFileItem.addActionListener(e -> openFile());
        saveFileItem.addActionListener(e -> {
            try {
                saveFile();
            } catch (PennSimException pennSimException) {
                pennSimException.showMessageDialog(getFrame());
            }
        });
        commandOutputWinItem.addActionListener( e -> commandOutputWindow.setVisible(true));
        quitItem.addActionListener(e -> confirmExit());

        fileMenu.add(newFileItem);
        fileMenu.add(openFileItem);
        fileMenu.add(saveFileItem);
        fileMenu.addSeparator();
        fileMenu.add(commandOutputWinItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);

        // Theme Menu
        lightItem.addActionListener(e -> setLookAndFeel("Light"));
        darkItem.addActionListener(e -> setLookAndFeel("Dark"));
        metalItem.addActionListener(e -> setLookAndFeel("Metal"));
        systemItem.addActionListener(e -> setLookAndFeel("System"));

        ButtonGroup group = new ButtonGroup();
        group.add(lightItem);
        group.add(darkItem);
        group.add(metalItem);
        group.add(systemItem);

        themeMenu.add(lightItem);
        themeMenu.add(darkItem);
        themeMenu.add(metalItem);
        themeMenu.add(systemItem);

        // Option Menu
        scrollLayoutItem.addActionListener(e -> {
            if (editorPanel.getTabLayoutPolicy() == JTabbedPane.WRAP_TAB_LAYOUT) {
                editorPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            } else {
                editorPanel.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
            }
        });
        optionsMenu.add(scrollLayoutItem);

        // Version Menu
        versionItem.setActionCommand("Version");
        versionItem.addActionListener(e ->
                JOptionPane.showMessageDialog(frame, PennSim.getVersion(), "Version", JOptionPane.INFORMATION_MESSAGE)
        );
        aboutMenu.add(versionItem);

        //Menu Bar
        menuBar.add(fileMenu);
        menuBar.add(themeMenu);
        menuBar.add(optionsMenu);
        menuBar.add(aboutMenu);
    }

    /**
     * Set up the Console Panel
     */
    private void setupControlPanel() {
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.25;
        newFileButton.addActionListener(e -> editorPanel.addFileTab());
        controlPanel.add(newFileButton, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 0.25;
        openFileButton.addActionListener(e -> openFile());
        controlPanel.add(openFileButton, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 0.25;
        saveFileButton.addActionListener(e -> {
            try {
                saveFile();
            } catch (PennSimException pennSimException) {
                pennSimException.showMessageDialog(getFrame());
            }
        });
        controlPanel.add(saveFileButton, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 3;
        constraints.gridy = 0;
        controlPanel.add(Box.createRigidArea(new Dimension(8, 8)), constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.weightx = 0.25;
        nextButton.addActionListener(e -> {
            try {
                machine.executeNext();
            } catch (GenericException genericException) {
                genericException.showMessageDialog(getFrame());
            }
        });
        controlPanel.add(nextButton, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 5;
        constraints.gridy = 0;
        constraints.weightx = 0.25;
        stepButton.addActionListener(e -> {
            try {
                machine.executeStep();
            } catch (GenericException genericException) {
                genericException.showMessageDialog(getFrame());
            }
        });
        controlPanel.add(stepButton, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 6;
        constraints.gridy = 0;
        constraints.weightx = 0.25;
        continueButton.addActionListener(e -> {
            try {
                machine.executeMany();
            } catch (GenericException genericException) {
                genericException.showMessageDialog(getFrame());
            }
        });
        controlPanel.add(continueButton, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 7;
        constraints.gridy = 0;
        constraints.weightx = 0.25;
        stopButton.addActionListener(e -> machine.stopExecution(true));
        controlPanel.add(stopButton, constraints);

        // TODO Have a dialog pop up confirming the action
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 8;
        constraints.gridy = 0;
        constraints.weightx = 0.25;
        resetButton.addActionListener(e -> resetDialog());
        controlPanel.add(resetButton, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 9;
        constraints.gridy = 0;
        constraints.weightx = 1;
        controlPanel.add(Box.createRigidArea(new Dimension(8, 8)), constraints);

        // Status Label
        constraints = new GridBagConstraints();
        constraints.gridx = 10;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.LINE_END;
        setStatusLabelSuspended();
        controlPanel.add(statusLabel, constraints);

//        controlPanel.setMinimumSize(new Dimension(700, 50));
        controlPanel.setPreferredSize(new Dimension(1050, 50));
        controlPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Set up border
        controlPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Controls"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5))
        );
        controlPanel.setVisible(true);
    }

    /**
     * Set up the Register Panel
     */
    private void setupRegisterPanel() {
//        registerPanel.setLayout(new GridBagLayout());
//
//        GridBagConstraints constraints = new GridBagConstraints();
//        constraints.fill = GridBagConstraints.BOTH;
//        constraints.gridx = 0;
//        constraints.gridy = 0;
//        constraints.gridwidth = 1;
//        constraints.gridheight = 1;
//        constraints.weightx = 1;
//        constraints.weighty = 1;
//        registerPanel.add(registerTable, constraints);
        registerPanel.add(registerTable, "Center");
        registerTable.getModel().addTableModelListener(this);
        registerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Registers"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        registerPanel.setVisible(true);
    }

    /**
     * Set up the Device Panel
     */
    private void setupDevicePanel() {
        devicePanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
//        constraints.weightx = 1;
        constraints.weighty = 1;
        devicePanel.add(video, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
//        constraints.weightx = 1;
        constraints.weighty = 1;
        devicePanel.add(ioPanel, constraints);

        devicePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Devices"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        devicePanel.setVisible(true);
    }

    /**
     *
     */
    private void setUpEditorPanel() {
        editorPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;

        editorPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Editor"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5))
        );
        editorPanel.setMinimumSize(new Dimension(400, 100));
        editorPanel.setVisible(true);
//        editorPanel.repaint();
//        editorPanel.addFileTab();
    }

    /**
     * Set up the Memory Panel
     */
    private void setupMemoryPanel() {
        memoryPanel.add(memoryScrollPane, "Center");
        memoryPanel.setMinimumSize(new Dimension(350, 100));
        memoryPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Memory"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5))
        );
        memoryTable.getModel().addTableModelListener(this);
        memoryTable.getModel().addTableModelListener(video);
        memoryTable.getModel().addTableModelListener((HighlightScrollBar) memoryScrollPane.getVerticalScrollBar());
        memoryTable.setPreferredScrollableViewportSize(new Dimension(350, 460));
    }

    /**
     *
     */
    private void setupTerminalPanel() {
        terminalPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        terminalPanel.add(commandPanel, constraints);

        terminalPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Terminal"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        terminalPanel.setVisible(true);
    }

    /**
     *
     */
    private void openFile() {
        int userSelection = fileChooser.showOpenDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getName();
            if (fileName.toLowerCase().endsWith(".obj")) {
                Console.println(machine.loadObjectFile(file));
            } else {
                this.editorPanel.addFileTab(file);
            }
        } else if (userSelection == JFileChooser.CANCEL_OPTION) {
            Console.println("Open command cancelled by user.");
        } else {
            Console.println("ERROR: Unknown selection. Could not open file.");
        }
    }

    /**
     *
     */
    private void saveFile() throws PennSimException {
        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            if (editorPanel.getSelectedComponent() instanceof EditorTab) {
                File fileToSave = ((EditorTab) editorPanel.getSelectedComponent()).getFile();
                Console.println("Save as file: " + fileToSave.getAbsolutePath());
            } else {
                throw new PennSimException("Unable to save the selected file.");
            }
        } else if (userSelection == JFileChooser.CANCEL_OPTION) {
            Console.println("Save command cancelled by user.");
        } else {
            Console.println("ERROR: Unknown selection. Could not save file.");
        }
    }

    /**
     * Scroll the Memory Panel to a specific row
     *
     * @param row the row to scroll to
     */
    public void scrollToIndex(int row) {
        memoryTable.scrollRectToVisible(memoryTable.getCellRect(row, 0, true));
    }

    /**
     * Scroll the Memory Panel to the row defined by the PC
     */
    public void scrollToPC() {
        scrollToPC(0);
    }

    public void scrollToPC(int row) {
        int address = machine.getRegisterFile().getPC() + row;
        memoryTable.scrollRectToVisible(memoryTable.getCellRect(address, 0, true));
    }

    public void tableChanged(TableModelEvent event) { }

    /**
     * Confirm exit when exiting the program
     */
    void confirmExit() {
        Object[] options = new Object[]{"Yes", "No"};
        int optionDialog = JOptionPane.showOptionDialog(frame, "Are you sure you want to quit?",
                "Quit verification", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (optionDialog == 0) {
            machine.cleanup();
            frame.setVisible(false);
            frame.dispose();
            System.exit(0);
        }
    }

    /**
     * Dialog which confirms reset of simulator
     */
    private void resetDialog() {
        // TODO investigate and fix the "Double Reset" issue
        String[] options = {"Yes", "No"};
        int answer = JOptionPane.showOptionDialog(
                null,
                "Do you want to reset the simulator?", "Reset Warning",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]);
        if (answer == 0) {
            Console.println(machine.stopExecution(true));
            reset();
            Console.println("System reset");
        }
    }

    /**
     * Get this JFrame
     *
     * @return the current JFrame
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Set the status label as "Running"
     */
    public void setStatusLabelRunning() {
        statusLabel.setText(statusLabelRunning);
        statusLabel.setForeground(runningColor);
    }

    /**
     * Set the status label as "Suspended"
     */
    public void setStatusLabelSuspended() {
        statusLabel.setText(statusLabelSuspended);
        statusLabel.setForeground(suspendedColor);
    }

    /**
     * Set the status label as "Halted"
     */
    public void setStatusLabelHalted() {
        statusLabel.setText(statusLabelHalted);
        statusLabel.setForeground(haltedColor);
    }

    /**
     * Set the status label as either "Suspended" or
     * "Running" based on the boolean input
     */
    public void setStatusLabel(boolean isSuspended) {
        if (isSuspended) {
            setStatusLabelSuspended();
        } else {
            setStatusLabelHalted();
        }

    }

    public void setTextConsoleEnabled(boolean enabled) {
        ioPanel.setEnabled(enabled);
    }

    /**
     * Reset the GUI
     */
    public void reset() {
        setTextConsoleEnabled(true);
        commandPanel.reset();
        video.reset();
        scrollToPC();
    }

    /**
     * Get the currently selected editor tab as an object.
     * @return The selected tab.
     */
    public EditorTab getSelectedTab() {
        return (EditorTab) this.editorPanel.getSelectedComponent();
    }
}
