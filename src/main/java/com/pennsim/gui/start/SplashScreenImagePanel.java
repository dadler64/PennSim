package com.pennsim.gui.start;

import com.pennsim.PennSim;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.JPanel;

public class SplashScreenImagePanel extends JPanel {

    static final int IMAGE_BORDER = 10;
    static final int IMAGE_WIDTH = 380;
    static final int IMAGE_HEIGHT = 100;

    private final Color fadeColor = new Color(255, 255, 255, 196);
    private final Color headerColor = new Color(50, 50, 50);
    private final Font headerFont = new Font("Monospaced", Font.BOLD, 72);
    private final Font versionFont = new Font("Serif", Font.PLAIN | Font.ITALIC, 32);
    private final Font copyrightFont = new Font("Serif", Font.ITALIC, 18);

    public SplashScreenImagePanel() {
        setLayout(null);

        int prefWidth = IMAGE_WIDTH + 2 * IMAGE_BORDER;
        int prefHeight = IMAGE_HEIGHT + 2 * IMAGE_BORDER;
        setPreferredSize(new Dimension(prefWidth, prefHeight));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        try {
            int x = IMAGE_BORDER;
            int y = IMAGE_BORDER;
            g.setColor(fadeColor);
            g.fillRect(x, y, IMAGE_WIDTH, IMAGE_HEIGHT);
            drawText(g, x, y);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void drawText(Graphics g, int x, int y) {
        FontMetrics fm;
        String str;

        g.setColor(headerColor);
        g.setFont(headerFont);
        g.drawString("PennSim", x, y + 45);
        g.setFont(copyrightFont);
        fm = g.getFontMetrics();
        str = "\u00a9 " + PennSim.COPYRIGHT_YEAR;
        g.drawString(str, x + IMAGE_WIDTH - fm.stringWidth(str), y + 16);
        g.setFont(versionFont);
        fm = g.getFontMetrics();
        str = PennSim.getVersion();
        g.drawString(str, x + IMAGE_WIDTH - fm.stringWidth(str), y + 75);
    }

}
