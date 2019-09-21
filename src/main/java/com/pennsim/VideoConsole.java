package com.pennsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class VideoConsole extends JPanel implements TableModelListener {

    private static final int START = 49152;
    private static final int NROWS = 128;
    private static final int NCOLS = 124;
    private static final int END = 65024;
    private static final int SCALING = 2;
    private static final int WIDTH = 256;
    private static final int HEIGHT = 248;
    private BufferedImage image;
    private Machine mac;

    public VideoConsole(Machine var1) {
        Dimension var2 = new Dimension(256, 248);
        this.setPreferredSize(var2);
        this.setMinimumSize(var2);
        this.setMaximumSize(var2);
        this.mac = var1;
        this.image = new BufferedImage(256, 248, 9);
        Graphics2D var3 = this.image.createGraphics();
        var3.setColor(Color.black);
        var3.fillRect(0, 0, 256, 248);
    }

    private static int convertToRGB(Word var0) {
        return (new Color(var0.getZext(14, 10) * 8, var0.getZext(9, 5) * 8, var0.getZext(4, 0) * 8))
                .getRGB();
    }

    public void reset() {
        Graphics2D var1 = this.image.createGraphics();
        var1.setColor(Color.black);
        var1.fillRect(0, 0, 256, 248);
        this.repaint();
    }

    public void tableChanged(TableModelEvent var1) {
        int var2 = var1.getFirstRow();
        int var3 = var1.getLastRow();
        if (var2 == 0 && var3 == 65535) {
            this.reset();
        } else {
            if (var2 < 49152 || var2 > 65024) {
                return;
            }

            byte var4 = 2;
            int var5 = var2 - '쀀';
            int var6 = var5 / 128 * var4;
            int var7 = var5 % 128 * var4;
            int var8 = convertToRGB(this.mac.getMemory().read(var2));

            for (int var9 = 0; var9 < var4; ++var9) {
                for (int var10 = 0; var10 < var4; ++var10) {
                    this.image.setRGB(var7 + var10, var6 + var9, var8);
                }
            }

            this.repaint(var7, var6, var4, var4);
        }

    }

    public void paintComponent(Graphics var1) {
        super.paintComponent(var1);
        Graphics2D var2 = (Graphics2D) var1;
        if (this.image == null) {
            int var3 = this.getWidth();
            int var4 = this.getHeight();
            this.image = (BufferedImage) this.createImage(var3, var4);
            Graphics2D var5 = this.image.createGraphics();
            var5.setColor(Color.white);
            var5.fillRect(0, 0, var3, var4);
        }

        var2.drawImage(this.image, null, 0, 0);
    }
}
