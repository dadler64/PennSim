package com.pennsim.gui.start;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

public class SplashScreen extends JWindow implements ActionListener {

    public static final int INITIAL_START_UP = 0;
    public static final int REGISTER_LC3_ISA = 1;
    public static final int REGISTER_P37X_ISA = 2;
    public static final int PENNSIM_GUI_INIT = 3;
    public static final int OPEN_ASM_FILE = 4;
    public static final int OPEN_OBJ_FILE = 5;

    private static final int PROGRESS_MAX = 3568;
    private static final boolean PRINT_TIMES = false;
    Marker[] markers = new Marker[]{
            new Marker(577, Strings.get("startup")),
            new Marker(1345, Strings.get("registerLC3")),
            new Marker(1345, Strings.get("registerP37X")),
            new Marker(2555, Strings.get("initGUI")),
            new Marker(3096, Strings.get("openASM")),
            new Marker(3096, Strings.get("openOBJ")),
    };
    boolean inClose = false; // for avoiding mutual recursion
    JProgressBar progress = new JProgressBar(INITIAL_START_UP, PROGRESS_MAX);
    JButton close = new JButton(Strings.get("startupCloseButton"));
    JButton cancel = new JButton(Strings.get("startupQuitButton"));
    long startTime = System.currentTimeMillis();

    public SplashScreen() {
        this.setAlwaysOnTop(true);
//        JPanel imagePanel = About.getImagePanel();
        JPanel imagePanel = new SplashScreenImagePanel();
        imagePanel.setBorder(null);

        progress.setStringPainted(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(close);
        close.addActionListener(this);
        buttonPanel.add(cancel);
        cancel.addActionListener(this);

        JPanel contents = new JPanel(new BorderLayout());
        contents.add(imagePanel, BorderLayout.NORTH);
        contents.add(progress, BorderLayout.CENTER);
        contents.add(buttonPanel, BorderLayout.SOUTH);
        contents.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        Color bg = imagePanel.getBackground();
        contents.setBackground(bg);
        buttonPanel.setBackground(bg);
        setBackground(bg);
        setContentPane(contents);
    }

    public void setProgress(int markerId) {
        final Marker marker = markers == null ? null : markers[markerId];
        if (marker != null) {
            SwingUtilities.invokeLater(() -> {
                progress.setString(marker.message);
                progress.setValue(marker.count);
            });
            if (PRINT_TIMES) {
                System.err.println((System.currentTimeMillis() - startTime) + " " + marker.message); //OK
            }
        } else {
            if (PRINT_TIMES) {
                System.err.println((System.currentTimeMillis() - startTime) + " ??"); //OK
            }
        }
    }

    @Override
    public void setVisible(boolean value) {
        if (value) {
            pack();
            Dimension dim = getToolkit().getScreenSize();
            int x = (int) (dim.getWidth() - getWidth()) / 2;
            int y = (int) (dim.getHeight() - getHeight()) / 2;
            setLocation(x, y);
        }
        super.setVisible(value);
    }

    public void close() {
        if (inClose) {
            return;
        }
        inClose = true;
        setVisible(false);
        inClose = false;
        if (PRINT_TIMES) {
            System.err.println((System.currentTimeMillis() - startTime) + " closed"); //OK
        }
        markers = null;
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cancel) {
            System.exit(0);
        } else if (src == close) {
            close();
        }
    }

    /**
     *
     */
    private static class Marker {

        int count;
        String message;

        Marker(int count, String message) {
            this.count = count;
            this.message = message;
        }
    }
}
