package com.pennsim.exception;

import com.pennsim.Word;

/**
 * Exception which is thrown when the user attempts to illegally access a position in memory
 */
public class IllegalMemoryAccessException extends GenericException {

    private final int address;

    public IllegalMemoryAccessException(int row) {
        this.address = row;
    }

    public String getExceptionDescription() {
        return Strings.get("illegalMemoryAccessException") + " " + Word.toHex(this.address) + "\n"
                + Strings.get("accessFail");
    }
}
