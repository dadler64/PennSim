package com.pennsim.gui;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;

public class EditorPanel extends JTabbedPane {

    private static int fileNumber = 0;

    public EditorPanel() {
        this.setMinimumSize(new Dimension(500, 100));
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Strings.get("titleEditor")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    /**
     * Add a new tab with an empty file.
     */
    public void addFileTab() {
        String fileName = Strings.get("untitled") + fileNumber;
        addFileTab(new EditorTab(new File(fileName)));
    }

    /**
     *
     * @param file
     */
    public void addFileTab(File file) {
        addFileTab(new EditorTab(file));
    }

    /**
     * Add a
     * @param tab
     */
    private void addFileTab(EditorTab tab) {
        String fileName = tab.getFilename();

        if (tab.getFile().isFile()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(tab.getFile()));
                String line = reader.readLine();

                while (line != null) {
                    tab.appendText(line + "\n");
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.add(fileName, tab);
        this.setTabComponentAt(this.indexOfTab(fileName), new ButtonTabComponent(this));

        this.setSelectedComponent(tab);

        fileNumber++;
    }

    public boolean rename(String fileName) {
        EditorTab selectedTab;
        if (this.getSelectedComponent() instanceof EditorTab) {
            selectedTab = (EditorTab) this.getSelectedComponent();
            selectedTab.rename(fileName);
        }

        return false;
    }
}
