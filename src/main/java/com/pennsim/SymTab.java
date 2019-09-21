package com.pennsim;

import java.util.Enumeration;
import java.util.Hashtable;

class SymTab {

    Hashtable table = new Hashtable();

    boolean insert(String var1, int var2) {
        if (this.lookup(var1) != -1) {
            return false;
        } else {
            this.table.put(var1, new Integer(var2));
            return true;
        }
    }

    int lookup(String var1) {
        Integer var2 = (Integer) this.table.get(var1);
        return var2 == null ? -1 : var2;
    }

    Enumeration get_labels() {
        return this.table.keys();
    }
}
