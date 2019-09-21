package com.pennsim;

public class IllegalMemAccessException extends ExceptionException {

    private int addr;

    public IllegalMemAccessException(int var1) {
        this.addr = var1;
    }

    public String getExceptionDescription() {
        return "com.pennsim.IllegalMemAccessException accessing address " + Word.toHex(this.addr)
                + "\n" + "(The MPR and PSR do not permit access to this address)";
    }
}
