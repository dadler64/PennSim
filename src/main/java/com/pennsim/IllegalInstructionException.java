package com.pennsim;

public class IllegalInstructionException extends ExceptionException {

    public IllegalInstructionException(String var1) {
        super(var1);
    }

    public String getExceptionDescription() {
        return "com.pennsim.IllegalInstructionException: " + this.getMessage();
    }
}
