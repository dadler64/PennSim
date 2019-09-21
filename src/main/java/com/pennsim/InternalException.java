package com.pennsim;

class InternalException extends RuntimeException {

    InternalException(String var1) {
        super("Internal Error: " + var1);
    }
}
