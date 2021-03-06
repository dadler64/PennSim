package com.pennsim.gui;

import com.pennsim.Machine;
import com.pennsim.Memory;
import com.pennsim.PennSim;
import com.pennsim.RegisterFile;
import com.pennsim.command.CommandLine;
import com.pennsim.exception.GenericException;
import com.pennsim.exception.PennSimException;
import com.pennsim.gui.generic.PFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
    private final JMenu menuFile;
    private final JMenu menuTheme;
    private final JMenu menuOptions;
    private final JMenu menuAbout;
    private final JMenuItem itemOpenFile;
    private final JMenuItem itemSaveFile;
    private final JMenuItem itemNewFile;
    private final JMenuItem itemQuit;
    private final JMenuItem itemCommandOutputWindow;
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
    private final JLabel statusLabel;
    private final Color runningColor;
    private final Color suspendedColor;
    private final Color haltedColor;
    private final TitledPanel registerPanel;
    private final TitledPanel memoryPanel;
    private final TitledPanel devicesPanel;
    private final TitledPanel terminalPanel;
    private final CommandLinePanel commandPanel;
    private final EditorPanel editorPanel;
    private final CommandOutputWindow commandOutputWindow;
    private final TextConsolePanel ioPanel;
    private final VideoConsole video;
    private final JTable registerTable;
    private final JTable memoryTable;
    private final Machine machine;
    private final JPanel leftPanel;
    private final JPanel mainPanel;
    private final PFrame frame;

    public GUI(final Machine machine, CommandLine commandLine) {
        this.frame = new PFrame("PennSim - " + PennSim.VERSION + " - " + PennSim.getISA());
        this.fileChooser = new JFileChooser(".");
        this.menuBar = new JMenuBar();
        this.menuFile = new JMenu(Strings.get("menuFile"));
        this.menuTheme = new JMenu(Strings.get("menuTheme"));
        this.menuOptions = new JMenu(Strings.get("menuOptions"));
        this.menuAbout = new JMenu(Strings.get("menuAbout"));
        this.itemNewFile = new JMenuItem(Strings.get("itemNewFile"));
        this.itemOpenFile = new JMenuItem(Strings.get("itemOpenFile"));
        this.itemSaveFile = new JMenuItem(Strings.get("itemSaveFile"));
        this.itemCommandOutputWindow = new JMenuItem(Strings.get("itemOutputWindow"));
        this.itemQuit = new JMenuItem(Strings.get("itemQuit"));
        this.lightItem = new JRadioButtonMenuItem(Strings.get("itemThemeLight"));
        this.darkItem = new JRadioButtonMenuItem(Strings.get("itemThemeDark"));
        this.metalItem = new JRadioButtonMenuItem(Strings.get("itemThemeMetal"));
        this.systemItem = new JRadioButtonMenuItem(Strings.get("itemThemeSystem"));
        this.scrollLayoutItem = new JCheckBoxMenuItem(Strings.get("itemSetLayoutScroll"));
        this.versionItem = new JMenuItem(Strings.get("itemSimulatorVersion"));
        this.nextButton = new JButton(Strings.get("buttonNext"));
        this.stepButton = new JButton(Strings.get("buttonStep"));
        this.continueButton = new JButton(Strings.get("buttonContinue"));
        this.stopButton = new JButton(Strings.get("buttonStop"));
        this.resetButton = new JButton(Strings.get("buttonReset"));
        this.newFileButton = new JButton(Strings.get("buttonNew"));
        this.openFileButton = new JButton(Strings.get("buttonOpen"));
        this.saveFileButton = new JButton(Strings.get("buttonSave"));
        this.statusLabel = new JLabel("");
        this.runningColor = new Color(43, 129, 51);
        this.suspendedColor = new Color(209, 205, 93);
        this.haltedColor = new Color(161, 37, 40);
        this.controlPanel = new TitledPanel(Strings.get("titleControl"));
        this.memoryPanel = new TitledPanel(Strings.get("titleMemory"));
        this.devicesPanel = new TitledPanel(Strings.get("titleDevices"));
        this.registerPanel = new TitledPanel(Strings.get("titleRegisters"));
        this.terminalPanel = new TitledPanel(Strings.get("titleTerminal"));
        this.editorPanel = new EditorPanel();
        this.leftPanel = new JPanel();
        this.mainPanel = new JPanel();
        this.machine = machine;

        // Register pane init
        RegisterFile registerFile = machine.getRegisterFile();
        registerTable = new JTable(registerFile);
        // Memory pane init
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
        // Command init
        commandOutputWindow = new CommandOutputWindow();
        commandPanel = new CommandLinePanel(machine, commandLine);
        commandPanel.setGUI(this);
        Console.registerConsole(commandPanel);
        // Devices  init
        ioPanel = new TextConsolePanel(machine.getMemory().getKeyBoardDevice(), machine.getMemory().getMonitor());
        video = new VideoConsole(machine);
    }

    /**
     * Set the UI theme
     *
     * @param lookAndFeel the desired theme you wish to use
     */
    public void setLookAndFeel(String lookAndFeel) {
        String theme;
        JFrame.setDefaultLookAndFeelDecorated(true);
        if (lookAndFeel != null) {
            switch (lookAndFeel) {
                case "Light":
                    if (!lightItem.isSelected()) {
                        lightItem.setSelected(true);
                    }
                    theme = "com.jtattoo.plaf.fast.FastLookAndFeel";
                    break;
                case "Dark":
                    if (!darkItem.isSelected()) {
                        darkItem.setSelected(true);
                    }
                    theme = "com.jtattoo.plaf.hifi.HiFiLookAndFeel"; // Dark Gray Theme
//                    theme = "com.jtattoo.plaf.noire.NoireLookAndFeel"; // Dark Theme
                    break;
                case "Metal":
                    if (!metalItem.isSelected()) {
                        metalItem.setSelected(true);
                    }
                    theme = UIManager.getCrossPlatformLookAndFeelClassName();
                    break;
                case "System":
                    if (!systemItem.isSelected()) {
                        systemItem.setSelected(true);
                    }
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
        setupDevicesPanel();
        setUpEditorPanel();
        setupMemoryPanel();
        setupTerminalPanel();
        setupCommandOutputWindow();

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
                return Strings.get("objectFileDescription");
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
                return Strings.get("assemblyFileDescription");
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
                return Strings.get("textFileDescription");
            }
        });
        registerTable.getModel().addTableModelListener(this);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;

        setLookAndFeel("System");

        frame.getContentPane().setLayout(new GridBagLayout());
        frame.setJMenuBar(menuBar);
        frame.getContentPane().add(mainPanel, constraints);
//        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("/PennSim.png")));
        // Original location of look and fel code
        frame.setMinimumSize(new Dimension(800, 750));
        frame.setPreferredSize(new Dimension(1050, 750));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                closeDialog();
            }

        });
        scrollToPC();
        commandPanel.actionPerformed(null);
    }

    private void setUpMainPanel() {
        mainPanel.setLayout(new BorderLayout());

        leftPanel.setLayout(new BorderLayout());
        leftPanel.setMaximumSize(new Dimension(256, Integer.MAX_VALUE));
        leftPanel.add(registerPanel, BorderLayout.PAGE_START);
        leftPanel.add(devicesPanel, BorderLayout.CENTER);

        mainPanel.add(controlPanel, BorderLayout.PAGE_START);
        mainPanel.add(leftPanel, BorderLayout.LINE_START);
        mainPanel.add(editorPanel, BorderLayout.CENTER);
        mainPanel.add(memoryPanel, BorderLayout.LINE_END);
        mainPanel.add(terminalPanel, BorderLayout.PAGE_END);
    }

    private void setUpMenuBar() {
        // File Menu
        itemNewFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        itemOpenFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        itemSaveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        itemQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));

        itemNewFile.addActionListener(e -> editorPanel.addFileTab());
        itemOpenFile.addActionListener(e -> openFile());
        itemSaveFile.addActionListener(e -> {
            try {
                saveFile();
            } catch (PennSimException pennSimException) {
                pennSimException.showMessageDialog(getFrame());
            }
        });
        itemCommandOutputWindow.addActionListener(e -> {
            commandOutputWindow.setVisible(true);
        });
        itemQuit.addActionListener(e -> closeDialog());

        menuFile.add(itemNewFile);
        menuFile.add(itemOpenFile);
        menuFile.add(itemSaveFile);
        menuFile.addSeparator();
        menuFile.add(itemCommandOutputWindow);
        menuFile.addSeparator();
        menuFile.add(itemQuit);

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

        menuTheme.add(lightItem);
        menuTheme.add(darkItem);
        menuTheme.add(metalItem);
        menuTheme.add(systemItem);

        // Option Menu
        scrollLayoutItem.addActionListener(e -> {
            if (editorPanel.getTabLayoutPolicy() == JTabbedPane.WRAP_TAB_LAYOUT) {
                editorPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            } else {
                editorPanel.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
            }
        });
        menuOptions.add(scrollLayoutItem);

        // Version Menu
        versionItem.setActionCommand("Version");
        versionItem.addActionListener(e ->
                JOptionPane.showMessageDialog(frame, PennSim.getVersion(), "Version", JOptionPane.INFORMATION_MESSAGE)
        );
        menuAbout.add(versionItem);

        //Menu Bar
        menuBar.add(menuFile);
        menuBar.add(menuTheme);
        menuBar.add(menuOptions);
        menuBar.add(menuAbout);
    }

    private void setupCommandOutputWindow() {
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
        Console.registerConsole(commandOutputWindow);
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
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Strings.get("titleControls")),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5))
        );
        controlPanel.setVisible(true);
    }

    /**
     * Set up the Register Panel
     */
    private void setupRegisterPanel() {
        TableColumn column = registerTable.getColumnModel().getColumn(0);
        column.setMaxWidth(35);
        column.setMinWidth(35);
        column = registerTable.getColumnModel().getColumn(2);
        column.setMaxWidth(35);
        column.setMinWidth(35);

        registerPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
//        constraints.weightx = 1;
//        constraints.weighty = 1;
        registerPanel.add(registerTable, constraints);
        registerTable.getModel().addTableModelListener(this);

        registerPanel.setVisible(true);
    }

    /**
     * Set up the Device Panel
     */
    private void setupDevicesPanel() {
        ioPanel.setMinimumSize(new Dimension(256, 256));

        devicesPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 0.2;
        devicesPanel.add(video, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 0.8;
        devicesPanel.add(ioPanel, constraints);
    }

    /**
     * Set up the Editor Panel
     */
    private void setUpEditorPanel() {
        editorPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 3;
        editorPanel.setMinimumSize(new Dimension(400, 100));
//        editorPanel.repaint();
//        editorPanel.addFileTab();
    }

    /**
     * Set up the Memory Panel
     */
    private void setupMemoryPanel() {
        memoryPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 3;
        constraints.weightx = 1;
        constraints.weighty = 1;

        TableColumn column = memoryTable.getColumnModel().getColumn(0);
        column.setMaxWidth(25);
        column.setMinWidth(25);
        column.setCellEditor(new DefaultCellEditor(new JCheckBox()));
        column = memoryTable.getColumnModel().getColumn(2);
        column.setMinWidth(60);
        column.setMaxWidth(60);

        JScrollPane memoryScrollPane = new JScrollPane(memoryTable) {
            public JScrollBar createVerticalScrollBar() {
                return new HighlightScrollBar(machine);
            }
        };
        memoryScrollPane.getVerticalScrollBar().setBlockIncrement(memoryTable.getModel().getRowCount() / 512);
        memoryScrollPane.getVerticalScrollBar().setUnitIncrement(30); // Scroll Speed
        memoryScrollPane.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);
        memoryScrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        memoryPanel.add(memoryScrollPane, constraints);
//        memoryPanel.add(memoryScrollPane, Component.CENTER_ALIGNMENT);
//        memoryPanel.setMinimumSize(new Dimension(350, 100));
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

        terminalPanel
                .setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Strings.get("titleTerminal")),
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
            Console.println(Strings.get("openCanceled"));
        } else {
            Console.println(Strings.get("unknownSelection") + Strings.get("openFail"));
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
                Console.println(Strings.get("saveAs") + " " + fileToSave.getAbsolutePath());
            } else {
                throw new PennSimException(Strings.get("saveFail"));
            }
        } else if (userSelection == JFileChooser.CANCEL_OPTION) {
            Console.println(Strings.get("saveCanceled"));
        } else {
            Console.println(Strings.get("unknownSelection") + Strings.get("saveFail"));
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

    public void tableChanged(TableModelEvent event) {
    }

    /**
     * Confirm exit when exiting the program
     */
    protected void closeDialog() {
        Object[] options = new Object[]{Strings.get("optionYes"), Strings.get("optionNo")};
        int optionDialog = JOptionPane
                .showOptionDialog(frame, Strings.get("quitPrompt"), Strings.get("quitTitle"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (optionDialog == JOptionPane.YES_OPTION) {
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
        String[] options = {Strings.get("optionYes"), Strings.get("optionNo")};
        int answer = JOptionPane.showOptionDialog(
                null, Strings.get("resetPrompt"), Strings.get("resetTitle"), JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (answer == JOptionPane.YES_OPTION) {
            Console.println(machine.stopExecution(true));
            reset();
            Console.println(Strings.get("resetMessage"));
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
        statusLabel.setText("    " + Strings.get("statusLabelRunning") + " ");
        statusLabel.setForeground(runningColor);
    }

    /**
     * Set the status label as "Suspended"
     */
    public void setStatusLabelSuspended() {
        statusLabel.setText(Strings.get("statusLabelSuspended") + " ");
        statusLabel.setForeground(suspendedColor);
    }

    /**
     * Set the status label as "Halted"
     */
    public void setStatusLabelHalted() {
        statusLabel.setText("       " + Strings.get("statusLabelHalted") + " ");
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
     *
     * @return The selected tab.
     */
    public EditorTab getSelectedTab() {
        return (EditorTab) this.editorPanel.getSelectedComponent();
    }
}
