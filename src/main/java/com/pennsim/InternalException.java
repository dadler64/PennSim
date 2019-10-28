package com.pennsim;

/**
 * Simple exception Class to handle Internal Exceptions
 */
class InternalException extends RuntimeException {

    InternalException(String message) {
        super("Internal Error: " + message);
    }
}
