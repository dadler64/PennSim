package com.pennsim;

/**
 * Exception which is thrown when the program comes upon an illegal instruction
 */
public class IllegalInstructionException extends GenericException {

    IllegalInstructionException(String message) {
        super(message);
    }

    public String getExceptionDescription() {
        return "Illegal Instruction Exception: " + this.getMessage();
    }
}
