package com.pennsim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class GUI implements ActionListener, TableModelListener {

    static final Color BreakPointColor = new Color(241, 103, 103);
    private static final Color PCColor;
    private static String LOOKANDFEEL = "Metal";

    static {
        PCColor = Color.YELLOW;
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
    private final JPanel leftPanel;
    private final JPanel controlPanel;
    private final JButton nextButton;
    private final String nextButtonCommand;
    private final JButton stepButton;
    private final String stepButtonCommand;
    private final JButton continueButton;
    private final String continueButtonCommand;
    private final JButton stopButton;
    private final String stopButtonCommand;
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
    //   private static String LOOKANDFEEL = "System";
//   private static String LOOKANDFEEL = "Motif";
//   private static String LOOKANDFEEL = "GTK+";
    private final JFrame frame;
    private final JPanel devicePanel;
    private final JPanel registerPanel;
    private final TextConsolePanel ioPanel;
    private final VideoConsole video;

    public GUI(final Machine machine, CommandLine var2) {
        this.frame = new JFrame("PennSim - " + PennSim.version + " - " + PennSim.getISA());
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
        this.leftPanel = new JPanel();
        this.controlPanel = new JPanel();
        this.nextButton = new JButton("Next");
        this.nextButtonCommand = "Next";
        this.stepButton = new JButton("Step");
        this.stepButtonCommand = "Step";
        this.continueButton = new JButton("Continue");
        this.continueButtonCommand = "Continue";
        this.stopButton = new JButton("Stop");
        this.stopButtonCommand = "Stop";
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
                            checkBox.setBackground(GUI.BreakPointColor);
                            checkBox.setForeground(GUI.BreakPointColor);
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
                        component.setBackground(GUI.PCColor);
                    } else if (GUI.this.machine.getMemory().isBreakPointSet(row)) {
                        component.setBackground(GUI.BreakPointColor);
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
        this.commandPanel = new CommandLinePanel(machine, var2);
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
        this.ioPanel = new TextConsolePanel(machine.getMemory().getKeyBoardDevice(),
                machine.getMemory().getMonitorDevice());
        this.ioPanel.setMinimumSize(new Dimension(256, 85));
        this.video = new VideoConsole(machine);
        this.commandPanel.setGUI(this);
    }

    public static void initLookAndFeel() {
        String theme = null;
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
            } catch (ClassNotFoundException var2) {
                ErrorLog.logError("Couldn't find class for specified look and feel:" + theme);
                ErrorLog.logError("Did you include the L&F library in the class path?");
                ErrorLog.logError("Using the default look and feel.");
            } catch (UnsupportedLookAndFeelException var3) {
                ErrorLog.logError(
                        "Can't use the specified look and feel (" + theme + ") on this platform.");
                ErrorLog.logError("Using the default look and feel.");
            } catch (Exception var4) {
                ErrorLog.logError(
                        "Couldn't get specified look and feel (" + theme + "), for some reason.");
                ErrorLog.logError("Using the default look and feel.");
                ErrorLog.logError(var4);
            }
        }

    }

    private void setupMemoryPanel() {
        this.memoryPanel.add(this.memScrollPane, "Center");
        this.memoryPanel.setMinimumSize(new Dimension(400, 100));
        this.memoryPanel.setBorder(BorderFactory
                .createCompoundBorder(BorderFactory.createTitledBorder("Memory"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.memTable.getModel().addTableModelListener(this);
        this.memTable.getModel().addTableModelListener(this.video);
        this.memTable.getModel().addTableModelListener(
                (HighlightScrollBar) this.memScrollPane.getVerticalScrollBar());
        this.memTable.setPreferredScrollableViewportSize(new Dimension(400, 460));
    }

    private void setupDevicePanel() {
        this.devicePanel.setLayout(new GridBagLayout());
        GridBagConstraints var1 = new GridBagConstraints();
        var1.fill = 10;
        var1.gridx = 0;
        var1.gridy = 0;
        var1.weightx = 1.0D;
        this.devicePanel.add(this.video, var1);
        var1 = new GridBagConstraints();
        var1.gridx = 0;
        var1.gridy = 1;
        var1.weightx = 1.0D;
        var1.fill = 0;
        this.devicePanel.add(this.ioPanel, var1);
        this.devicePanel.setBorder(BorderFactory
                .createCompoundBorder(BorderFactory.createTitledBorder("Devices"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.devicePanel.setVisible(true);
    }

    private void setupControlPanel() {
        boolean var1 = true;
        this.controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints grid = new GridBagConstraints();
        grid.fill = 2;
        this.nextButton.setActionCommand("Next");
        this.nextButton.addActionListener(this);
        grid.weightx = 1.0D;
        grid.gridx = 0;
        grid.gridy = 0;
        this.controlPanel.add(this.nextButton, grid);
        this.stepButton.setActionCommand("Step");
        this.stepButton.addActionListener(this);
        grid.gridx = 1;
        grid.gridy = 0;
        this.controlPanel.add(this.stepButton, grid);
        this.continueButton.setActionCommand("Continue");
        this.continueButton.addActionListener(this);
        grid.gridx = 2;
        grid.gridy = 0;
        this.controlPanel.add(this.continueButton, grid);
        this.stopButton.setActionCommand("Stop");
        this.stopButton.addActionListener(this);
        grid.gridx = 3;
        grid.gridy = 0;
        this.controlPanel.add(this.stopButton, grid);
        grid.gridx = 4;
        grid.gridy = 0;
        grid.fill = 0;
        grid.anchor = 22;
        this.setStatusLabelSuspended();
        this.controlPanel.add(this.statusLabel, grid);
        grid = new GridBagConstraints();
        grid.gridx = 0;
        grid.gridy = 1;
        grid.gridwidth = 6;
        this.controlPanel.add(Box.createRigidArea(new Dimension(5, 5)), grid);
        grid = new GridBagConstraints();
        grid.gridx = 0;
        grid.gridy = 2;
        grid.gridwidth = 6;
        grid.gridheight = 1;
        grid.ipady = 100;
        grid.weightx = 1.0D;
        grid.weighty = 1.0D;
        grid.fill = 1;
        this.controlPanel.add(this.commandPanel, grid);
        this.controlPanel.setMinimumSize(new Dimension(100, 150));
        this.controlPanel.setPreferredSize(new Dimension(100, 150));
        this.controlPanel.setBorder(BorderFactory
                .createCompoundBorder(BorderFactory.createTitledBorder("Controls"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.controlPanel.setVisible(true);
    }

    private void setupRegisterPanel() {
        this.registerPanel.setLayout(new GridBagLayout());
        GridBagConstraints grid = new GridBagConstraints();
        grid.gridx = 0;
        grid.gridy = 0;
        grid.weightx = 1.0D;
        grid.fill = 2;
        this.registerPanel.add(this.regTable, grid);
        this.registerPanel.setBorder(BorderFactory
                .createCompoundBorder(BorderFactory.createTitledBorder("Registers"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.registerPanel.setVisible(true);
    }

    void setUpGUI() {
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
        GridBagConstraints grid = new GridBagConstraints();
        grid.fill = 1;
        grid.gridx = 0;
        grid.gridy = 0;
        grid.gridwidth = 2;
        grid.weighty = 1.0D;
        grid.gridwidth = 0;
        this.frame.getContentPane().add(this.controlPanel, grid);
        grid = new GridBagConstraints();
        grid.gridx = 0;
        grid.gridy = 1;
        grid.gridwidth = 1;
        grid.gridheight = 1;
        grid.weightx = 0.0D;
        grid.fill = 2;
        this.frame.getContentPane().add(this.registerPanel, grid);
        grid = new GridBagConstraints();
        grid.gridx = 0;
        grid.gridy = 2;
        grid.weightx = 0.0D;
        grid.gridheight = 1;
        grid.gridwidth = 1;
        grid.fill = 1;
        this.frame.getContentPane().add(this.devicePanel, grid);
        grid = new GridBagConstraints();
        grid.gridx = 1;
        grid.gridy = 1;
        grid.gridheight = 2;
        grid.gridwidth = 0;
        grid.fill = 1;
        grid.weightx = 1.0D;
        this.frame.getContentPane().add(this.memoryPanel, grid);
        this.frame.setSize(new Dimension(700, 725));
        this.frame.setDefaultCloseOperation(3);
        this.frame.pack();
        this.frame.setVisible(true);
        this.scrollToPC();
        this.commandPanel.actionPerformed(null);
    }

    void scrollToIndex(int row) {
        this.memTable.scrollRectToVisible(this.memTable.getCellRect(row, 0, true));
    }

    void scrollToPC() {
        this.scrollToPC(0);
    }

    void scrollToPC(int pc) {
        int var2 = this.machine.getRegisterFile().getPC() + pc;
        this.memTable.scrollRectToVisible(this.memTable.getCellRect(var2, 0, true));
    }

    public void tableChanged(TableModelEvent event) {
        if (!this.machine.isContinueMode()) {
        }

    }

    void confirmExit() {
        Object[] obj = new Object[]{"Yes", "No"};
        int optionDialog = JOptionPane
                .showOptionDialog(this.frame, "Are you sure you want to quit?", "Quit verification",
                        0, 3,
                        null, obj, obj[1]);
        if (optionDialog == 0) {
            this.machine.cleanup();
            System.exit(0);
        }

    }

    public void actionPerformed(ActionEvent event) {
        try {
            int var2;
            try {
                var2 = Integer.parseInt(event.getActionCommand());
                this.scrollToIndex(var2);
            } catch (NumberFormatException var4) {
                if ("Next".equals(event.getActionCommand())) {
                    this.machine.executeNext();
                } else if ("Step".equals(event.getActionCommand())) {
                    this.machine.executeStep();
                } else if ("Continue".equals(event.getActionCommand())) {
                    this.machine.executeMany();
                } else if ("Quit".equals(event.getActionCommand())) {
                    this.confirmExit();
                } else if ("Stop".equals(event.getActionCommand())) {
                    Console.println(this.machine.stopExecution(true));
                } else if ("OutputWindow".equals(event.getActionCommand())) {
                    this.commandOutputWindow.setVisible(true);
                } else if ("Version".equals(event.getActionCommand())) {
                    JOptionPane.showMessageDialog(this.frame, PennSim.getVersion(), "Version", 1);
                } else if ("Open".equals(event.getActionCommand())) {
                    var2 = this.fileChooser.showOpenDialog(this.frame);
                    if (var2 == 0) {
                        File file = this.fileChooser.getSelectedFile();
                        Console.println(this.machine.loadObjectFile(file));
                    } else {
                        Console.println("Open command cancelled by user.");
                    }
                }
            }
        } catch (ExceptionException e) {
            e.showMessageDialog(this.frame);
        }

    }

    public JFrame getFrame() {
        return this.frame;
    }

    void setStatusLabelRunning() {
        this.statusLabel.setText("    Running ");
        this.statusLabel.setForeground(this.runningColor);
    }

    void setStatusLabelSuspended() {
        this.statusLabel.setText("Suspended ");
        this.statusLabel.setForeground(this.suspendedColor);
    }

    void setStatusLabelHalted() {
        this.statusLabel.setText("       Halted ");
        this.statusLabel.setForeground(this.haltedColor);
    }

    public void setStatusLabel(boolean var1) {
        if (var1) {
            this.setStatusLabelSuspended();
        } else {
            this.setStatusLabelHalted();
        }

    }

    void setTextConsoleEnabled(boolean enabled) {
        this.ioPanel.setEnabled(enabled);
    }

    public void reset() {
        this.setTextConsoleEnabled(true);
        this.commandPanel.reset();
        this.video.reset();
        this.scrollToPC();
    }
}
