package com.pennsim.gui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class TitledPanel extends JPanel {

    public TitledPanel(String title) {
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.setVisible(true);
    }
}
