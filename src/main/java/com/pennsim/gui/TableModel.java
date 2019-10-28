package com.pennsim.gui;

import com.pennsim.PennSim;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

public abstract class TableModel extends AbstractTableModel {

    public void fireTableCellUpdated(int var1, int var2) {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableCellUpdated(var1, var2);
        }

    }

    public void fireTableChanged(TableModelEvent var1) {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableChanged(var1);
        }

    }

    public void fireTableDataChanged() {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableDataChanged();
        }

    }

    public void fireTableRowsUpdated(int var1, int var2) {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableRowsUpdated(var1, var2);
        }

    }

    public void fireTableRowsInserted(int var1, int var2) {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableRowsInserted(var1, var2);
        }

    }

    public void fireTableRowsDeleted(int var1, int var2) {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableRowsDeleted(var1, var2);
        }

    }

    public void fireTableStructureChanged() {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableStructureChanged();
        }

    }
}
