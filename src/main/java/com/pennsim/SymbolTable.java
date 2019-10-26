package com.pennsim;

import java.util.Enumeration;
import java.util.Hashtable;

class SymbolTable {

    private Hashtable<String, Integer> table = new Hashtable<>();

    /**
     * Insert data into the symbol table
     */
    boolean insert(String var1, int var2) {
        if (this.lookup(var1) != -1) {
            return false;
        } else {
            this.table.put(var1, var2);
            return true;
        }
    }

    int lookup(String label) {
        Integer var2 = this.table.get(label);
        return var2 == null ? -1 : var2;
    }

    Enumeration get_labels() {
        return this.table.keys();
    }
}
