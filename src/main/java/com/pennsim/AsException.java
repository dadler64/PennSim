package com.pennsim;

class AsException extends Exception {

    public Instruction insn;

    AsException(Instruction var1, String var2) {
        super(var2);
        this.insn = var1;
    }

    AsException(String var1) {
        super(var1);
    }

    public String getMessage() {
        String var1 = "Assembly error: ";
        if (this.insn != null) {
            var1 = var1 + "[line " + this.insn.getLineNumber() + ", '" + this.insn.getOriginalLine()
                    + "']: ";
        }

        var1 = var1 + super.getMessage();
        return var1;
    }
}
