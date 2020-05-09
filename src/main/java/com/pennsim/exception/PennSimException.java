package com.pennsim.exception;

/**
 * Simple exception Class to handle Internal Exceptions
 */
public class PennSimException extends GenericException {

    public PennSimException(String message) {
        super("Error: " + message);
    }
}
