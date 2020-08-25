package com.pennsim.exception;

/**
 * Simple exception Class to handle Internal Exceptions
 */
public class InternalException extends RuntimeException {

    public InternalException(String message) {
        super(Strings.get("internalError") + ": " + message);
    }
}
