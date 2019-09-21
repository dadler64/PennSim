package com.pennsim;

import java.awt.Color;
import java.awt.Cursor;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class HighlightScrollBar extends JScrollBar implements TableModelListener {

    private static final int MARK_HEIGHT = 4;
    private static final int SCROLL_BUTTON_SIZE = 15;
    private Map highlights = new Hashtable();
    private double scaleFactor = 1.0D;
    private JButton PCButton;
    private Machine mac;

    public HighlightScrollBar(Machine var1) {
        this.mac = var1;
    }

    public void tableChanged(TableModelEvent var1) {
        javax.swing.table.TableModel var2 = (javax.swing.table.TableModel) var1.getSource();
        this.scaleFactor = (double) var2.getRowCount() / (double) (this.getHeight() - 30);
        int var3 = var1.getFirstRow();
        JButton var4;
        if (var2.getValueAt(var3, 0).equals(Boolean.TRUE)) {
            var4 = new JButton();
            var4.setToolTipText((String) var2.getValueAt(var3, 1));
            var4.setActionCommand(String.valueOf(var3));
            var4.addActionListener(this.mac.getGUI());
            var4.setSize(this.getWidth() - 5, 4);
            var4.setForeground(GUI.BreakPointColor);
            var4.setBackground(GUI.BreakPointColor);
            var4.setBorder(BorderFactory.createLineBorder(Color.RED));
            var4.setOpaque(true);
            var4.setCursor(new Cursor(12));
            var4.setLocation(3, (int) ((double) var3 / this.scaleFactor) + 15);
            this.highlights.put(var3, var4);
            this.add(var4);
        } else {
            assert var2.getValueAt(var3, 0).equals(Boolean.FALSE);

            var4 = (JButton) this.highlights.remove(var3);
            if (var4 != null) {
                this.remove(var4);
            }
        }

        this.repaint(0L, 0, (int) ((double) var3 / this.scaleFactor) + 15, this.getWidth(), 4);
    }
}
