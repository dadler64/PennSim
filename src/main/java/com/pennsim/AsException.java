package com.pennsim;

/**
 * Custom exception class for when an exception is raised during assembly
 */
class AsException extends Exception {

    private Instruction instruction;

    AsException(Instruction instruction, String message) {
        super(message);
        this.instruction = instruction;
    }

    AsException(String message) {
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
