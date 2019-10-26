package com.pennsim;

import java.awt.Container;
import javax.swing.JOptionPane;

/**
 * Generic exception class to help handle the most generic of exceptions
 */
public abstract class GenericException extends Exception {

    GenericException() {
    }

    GenericException(String message) {
        super(message);
    }

    public String getExceptionDescription() {
        return "Generic Exception: " + this.getMessage();
    }

    void showMessageDialog(Container container) {
        JOptionPane.showMessageDialog(container, this.getExceptionDescription());
        Console.println("Exception: " + this.getExceptionDescription());
    }
}