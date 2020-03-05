package com.pennsim.gui;

import com.pennsim.Machine;
import com.pennsim.Word;
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
    private static final int NUMBER_OF_ROWS = 128;
    private static final int NUMBER_OF_COLUMNS = 124;
    private static final int END = 65024;
    private static final int SCALING = 2;
    private static final int WIDTH = 256;
    private static final int HEIGHT = 248;
    private BufferedImage image;
    private Machine machine;

    VideoConsole(Machine machine) {
        Dimension dimension = new Dimension(WIDTH, HEIGHT);
        this.setPreferredSize(dimension);
        this.setMinimumSize(dimension);
        this.setMaximumSize(dimension);
        this.machine = machine;
        this.image = new BufferedImage(WIDTH, HEIGHT, 9);
        Graphics2D graphics2D = this.image.createGraphics();
        graphics2D.setColor(Color.black);
        graphics2D.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private static int convertToRGB(Word word) {
        return (new Color(word.getZext(14, 10) * 8, word.getZext(9, 5) * 8, word.getZext(4, 0) * 8)).getRGB();
    }

    public void reset() {
        Graphics2D graphics2D = this.image.createGraphics();
        graphics2D.setColor(Color.black);
        graphics2D.fillRect(0, 0, WIDTH, HEIGHT);
        this.repaint();
    }

    public void tableChanged(TableModelEvent tableModelEvent) {
        int firstRow = tableModelEvent.getFirstRow();
        int lastRow = tableModelEvent.getLastRow();
        if (firstRow == 0 && lastRow == 65535) {
            this.reset();
        } else {
            if (firstRow < START || firstRow > END) {
                return;
            }

            byte scaling = SCALING;
            int start = firstRow - START;
            int yPos = start / NUMBER_OF_ROWS * scaling;
            int xPos = start % NUMBER_OF_COLUMNS * scaling;
            int rgb = convertToRGB(this.machine.getMemory().read(firstRow));

            for (int i = 0; i < scaling; ++i) {
                for (int j = 0; j < scaling; ++j) {
                    this.image.setRGB(xPos + j, yPos + i, rgb);
                }
            }

            this.repaint(xPos, yPos, scaling, scaling);
        }

    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics;
        if (this.image == null) {
            int width = this.getWidth();
            int height = this.getHeight();
            this.image = (BufferedImage) this.createImage(width, height);
            Graphics2D imageGraphics2D = this.image.createGraphics();
            imageGraphics2D.setColor(Color.white);
            imageGraphics2D.fillRect(0, 0, width, height);
        }

        graphics2D.drawImage(this.image, null, 0, 0);
    }
}
