package com.pennsim;

import java.util.Enumeration;
import java.util.Hashtable;

public class SymbolTable {

    private Hashtable<String, Integer> table = new Hashtable<>();

    /**
     * Insert data into the symbol table
     */
    public boolean insert(String label, int address) {
        if (this.lookup(label) != -1) {
            return false;
        } else {
            this.table.put(label, address);
            return true;
        }
    }

    public int lookup(String label) {
        Integer address = this.table.get(label);
        return address == null ? -1 : address;
    }

    public Enumeration<String> getLabels() {
        return this.table.keys();
    }
}
