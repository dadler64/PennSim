package com.pennsim;

import java.util.Enumeration;
import java.util.Hashtable;

class SymbolTable {

    private Hashtable<String, Integer> table = new Hashtable<>();

    /**
     * Insert data into the symbol table
     */
    boolean insert(String symbol, int address) {
        if (this.lookup(symbol) != -1) {
            return false;
        } else {
            this.table.put(symbol, address);
            return true;
        }
    }

    /**
     * Get the address of a symbol in the table
     */
    int lookup(String symbol) {
        Integer address = this.table.get(symbol);
        return address == null ? -1 : address;
    }

    /**
     * Get an enumeration of all of the symbols
     */
    Enumeration getSymbols() {
        return this.table.keys();
    }
}
