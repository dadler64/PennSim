package com.pennsim;

import java.awt.Container;
import javax.swing.JOptionPane;

public abstract class ExceptionException extends Exception {

    public ExceptionException() {
    }

    public ExceptionException(String var1) {
        super(var1);
    }

    public String getExceptionDescription() {
        return "Generic Exception: " + this.getMessage();
    }

    public void showMessageDialog(Container var1) {
        JOptionPane.showMessageDialog(var1, this.getExceptionDescription());
        Console.println("Exception: " + this.getExceptionDescription());
    }
}
