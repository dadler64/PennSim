package com.pennsim.gui;

import com.pennsim.PennSim;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class EditorTab extends JPanel {
    private final File file;
    private final RSyntaxTextArea textArea;

    public EditorTab(File file) {
        super(true);
        this.file = file;
        this.setLayout(new GridBagLayout());

        RSyntaxDocument document = new RSyntaxDocument(SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86);
        textArea = new RSyntaxTextArea(document);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane scrollPane = new RTextScrollPane(textArea);

        // TODO develop syntax highlighting
        // Set syntax highlighting
//        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
//        atmf.putMapping("text/lc3", "com.pennsim.util.LC3Syntax");
//        textArea.setSyntaxEditingStyle("text/lc3");

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;

        this.add(scrollPane, constraints);
    }

    /**
     *
     * @return
     */
    public String getFilename() {
        return file.getName();
    }

    /**
     *
     * @return
     */
    public File getFile() {
        return file;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public boolean rename(String fileName) {
        return this.file.renameTo(new File(fileName));
    }

    public String getText() {
        return this.textArea.getText();
    }

    public String getText(int startPos, int endPos) {
        String selectedText = "";
        try {
            int length = this.textArea.getText().length();
            if (endPos < length ) {
                selectedText = this.textArea.getText(startPos, endPos - startPos);
            } else {
                selectedText = this.textArea.getText(startPos, (endPos - startPos) - (endPos - length));
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return selectedText;
    }

    public void cut() {
        this.textArea.cut();
    }

    public void copy() {
        this.textArea.copy();
    }

    public void paste() {
        this.textArea.paste();
    }

    public void appendText(String text) {
        this.textArea.append(text);
    }
}
