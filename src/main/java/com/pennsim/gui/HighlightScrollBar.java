package com.pennsim.gui;

import com.pennsim.Machine;
import java.awt.Color;
import java.awt.Cursor;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * Class which represents the highlighted scroll bar in the Memory Panel
 */
public class HighlightScrollBar extends JScrollBar implements TableModelListener {

    private static final int MARK_HEIGHT = 4;
    private static final int SCROLL_BUTTON_SIZE = 15;
    private Map<Integer, JButton> highlights = new Hashtable<>();
    private Machine machine;
    //    private JButton PCButton;

    HighlightScrollBar(Machine machine) {
        this.machine = machine;
    }

    public void tableChanged(TableModelEvent event) {
        javax.swing.table.TableModel model = (javax.swing.table.TableModel) event.getSource();
        double scaleFactor = (double) model.getRowCount() / (double) (this.getHeight() - 30);
        int firstRow = event.getFirstRow();
        JButton button;
        if (model.getValueAt(firstRow, 0).equals(Boolean.TRUE)) {
            button = new JButton();
            button.setToolTipText((String) model.getValueAt(firstRow, 1));
            button.setActionCommand(String.valueOf(firstRow));
            button.addActionListener(this.machine.getGUI());
            button.setSize(this.getWidth() - 5, MARK_HEIGHT);
            button.setForeground(GUI.BREAK_POINT_COLOR);
            button.setBackground(GUI.BREAK_POINT_COLOR);
            button.setBorder(BorderFactory.createLineBorder(Color.RED));
            button.setOpaque(true);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setLocation(3, (int) ((double) firstRow / scaleFactor) + SCROLL_BUTTON_SIZE);
            this.highlights.put(firstRow, button);
            this.add(button);
        } else {
            assert model.getValueAt(firstRow, 0).equals(Boolean.FALSE);

            button = this.highlights.remove(firstRow);
            if (button != null) {
                this.remove(button);
            }
        }

        this.repaint(0L, 0, (int) ((double) firstRow / scaleFactor) + SCROLL_BUTTON_SIZE, this.getWidth(), MARK_HEIGHT);
    }
}
