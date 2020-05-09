package com.pennsim.exception;

import com.pennsim.isa.Instruction;

/**
 * Custom exception class for when an exception is raised during assembly
 */
public class AsException extends Exception {

    private Instruction instruction;

    public AsException(Instruction instruction, String message) {
        super(message);
        this.instruction = instruction;
    }

    public AsException(String message) {
        super(message);
    }

    public String getMessage() {
        String message = "Assembly error: ";
        if (this.instruction != null) {
            message = message + "[line " + this.instruction.getLineNumber() + ", '" + this.instruction.getOriginalLine()
                    + "']: ";
        }

        message = message + super.getMessage();
        return message;
    }
}
