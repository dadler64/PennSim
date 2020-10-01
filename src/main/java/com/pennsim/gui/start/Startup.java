package com.pennsim.gui.start;

import com.adlerd.logger.Logger;
import com.pennsim.Machine;
import com.pennsim.PennSim;
import com.pennsim.TempRun;
import com.pennsim.command.CommandLine;
import com.pennsim.gui.GUI;
import com.pennsim.util.LocaleManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.SwingUtilities;

public class Startup {

    private final boolean isTerminalMode;
    private static Startup startupTemp = null;
    private final ArrayList<File> filesToOpen = new ArrayList<>();
    private boolean showSplash;
    private File loadFile;
    private boolean initialized = false;
    private SplashScreen monitor = null;
    private Machine machine = null;
    private CommandLine commandLine = null;
    private GUI gui = null;

    private Startup(boolean isTerminalMode) {
        this.isTerminalMode = isTerminalMode;
        this.showSplash = !isTerminalMode;
    }

    static void doOpen(File file) {
        if (startupTemp != null) {
            startupTemp.doOpenFile(file);
        }
    }

    private static void registerHandler() {
        try {
            Class<?> needed1 = Class.forName("com.apple.eawt.Application");
            if (needed1 == null) {
                return;
            }
            Class<?> needed2 = Class.forName("com.apple.eawt.ApplicationAdapter");
            if (needed2 == null) {
                return;
            }
            MacOsAdapter.register();
            MacOsAdapter.addListeners(true);
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable t) {
            t.printStackTrace();
            try {
                MacOsAdapter.addListeners(false);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private static void setLocale(String lang) {
        Locale[] options = Strings.getLocaleOptions();
        for (Locale option : options) {
            if (lang.equals(option.toString())) {
                LocaleManager.setLocale(option);
                return;
            }
        }
        System.err.println(Strings.get("invalidLocaleError")); //OK
        System.err.println(Strings.get("invalidLocaleOptionsHeader")); //OK
        for (Locale option : options) {
            System.err.println("   " + option.toString()); //OK
        }
        System.exit(-1);
    }

    public static Startup parseArgs(String[] args) {
        // see whether we'll be using any graphics
        boolean isTerminalMode = false;
        boolean isClearPreferences = false;

        for (String arg : args) {
            if (arg.equals("-t")) {
                isTerminalMode = true;
                break;
            }
        }

        if (!isTerminalMode) {
            // we're using the GUI: Set up the Look&Feel to match the platform
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Logisim");
            System.setProperty("apple.laf.useScreenMenuBar", "true");

            LocaleManager.setReplaceAccents(false);
        }

        for (String arg : args) {
            if (arg.equalsIgnoreCase("-t")) {
                isTerminalMode = true;
            } else if (arg.equalsIgnoreCase("-lc3")) {
                PennSim.LC3 = true;
            } else if (arg.equalsIgnoreCase("-p37x")) {
                PennSim.P37X = true;
            } else if (arg.equalsIgnoreCase("-pipeline")) {
                PennSim.PIPELINE_MODE = true;
            } else {
                System.out.println("Arg '" + arg + "' not recognized");
                printUsage();
            }
        }

        if (PennSim.LC3 && PennSim.P37X) {
            System.err.println("Error: can't specify more than one ISA");
            printUsage();
        } else if (!PennSim.LC3 && !PennSim.P37X) {
            System.err.println("Error: ISA not specified");
            printUsage();
        }

        Startup startup = new Startup(isTerminalMode);
        startupTemp = startup;

        return startup;
    }

    /**
     * Print out the usage of the command line functions for PennSim
     */
    private static void printUsage() {
        System.out.println("\nUsage: java -jar PennSim [-lc3] [-p37x] [-pipeline] [-t] [-s script]");
        System.out.println("\t-lc3 : simulate the LC-3 ISA");
        System.out.println("\t-p37x : simulate the P37X ISA");
        System.out.println("\t-pipeline : simulate a 5-stage fully-bypassed pipeline");
        System.out.println("\t-t : start in command-line mode");
        System.out.println("\t-s script : run 'script' from a script file");
    }

    private void doOpenFile(File file) {
        if (initialized) {
            System.err.println("Need to implement file loading...");
        } else {
            filesToOpen.add(file);
        }
    }

    List<File> getFilesToOpen() {
        return filesToOpen;
    }

    File getLoadFile() {
        return loadFile;
    }

    public void run() {
        if (isTerminalMode) {
            try {
                TerminalModeInterface.run(this);
                return;
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(-1);
                return;
            }
        }

        // kick off the progress monitor
        // (The values used for progress values are based on a single run where
        // I loaded a large file.)
        if (showSplash) {
//            System.err.println("STARTING SPLASHSCREEN!");
            try {
                monitor = new SplashScreen();
                monitor.setVisible(true);
            } catch (ExceptionInInitializerError t) {
                t.printStackTrace();
                monitor = null;
                showSplash = false;
            }
        }

        // pre-load the two basic component libraries, just so that the time
        // taken is shown separately in the progress bar.
        if (showSplash) {
            String isa = PennSim.getISA();
            if (isa != null) {
                if (isa.startsWith("LC3")) {
                    monitor.setProgress(SplashScreen.REGISTER_LC3_ISA);

                } else if (isa.startsWith("P37X")) {
                    monitor.setProgress(SplashScreen.REGISTER_P37X_ISA);
                } else {
                    monitor.close();
                    System.exit(66);
                }
            }
        }

        machine = new Machine();
        commandLine = new CommandLine(machine);

        // now that the splash screen is almost gone, we do some last-minute
        // interface initialization
        if (showSplash) {
            monitor.setProgress(SplashScreen.PENNSIM_GUI_INIT);
        }

        System.out.println("Loading graphical interface\n");
        gui = new GUI(machine, commandLine);
        machine.setGUI(gui);

        // if user has double-clicked a file to open, we'll
        // use that as the file to open now.
        initialized = true;

        // load file
        if (filesToOpen.isEmpty()) {
//            ProjectActions.doNew(monitor, true);
            Logger.debugln("No file to load!");
            if (showSplash) {
                monitor.close();
            }
            SwingUtilities.invokeLater(new TempRun(gui));
        } else {
            System.err.println("Need to implement file handling and loading...");
//            boolean first = true;
//            for (File fileToOpen : filesToOpen) {
//                try {
//                    ProjectActions.doOpen(monitor, fileToOpen, substitutions);
//                } catch (LoadFailedException ex) {
//                    errorln(fileToOpen.getName() + ": " + ex.getMessage()); //OK
//                    System.exit(-1);
//                }
//                if (first) {
//                    first = false;
//                    if (showSplash) {
//                        monitor.close();
//                    }
//                    monitor = null;
//                }
//            }
        }
    }
}
