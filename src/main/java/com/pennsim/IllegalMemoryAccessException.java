package com.pennsim;

/**
 * Exception which is thrown when the user attempts to illegally access a position in memory
 */
public class IllegalMemoryAccessException extends GenericException {

    private int address;

    IllegalMemoryAccessException(int row) {
        this.address = row;
    }

    public String getExceptionDescription() {
        return "Illegal Memory Access Exception thrown while accessing address " + Word.toHex(this.address)
                + "\n(The MPR and PSR do not permit access to this address)";
    }
}
