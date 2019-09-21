package com.pennsim;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

class Assembler {

    String as(String[] var1) throws AsException {
        String var3 = null;
        SymTab var4 = new SymTab();

        for (int var5 = 0; var5 < var1.length; ++var5) {
            if (var1[var5].length() == 0) {
                throw new AsException("Null arguments are not permitted.");
            }

            var3 = var1[var5];
        }

        if (var3 != null && var3.length() != 0) {
            String var6 = this.base_filename(var3);
            List var2 = this.parse(var6);
            var2 = this.pass_zero(var2);
            this.pass_one(var4, var2);
            this.pass_two(var4, var2, var6);
            this.gen_sym(var4, var2, var6);
            return "";
        } else {
            throw new AsException("No .asm file specified.");
        }
    }

    String base_filename(String var1) throws AsException {
        if (!var1.endsWith(".asm")) {
            throw new AsException("Input file must have .asm suffix ('" + var1 + "')");
        } else {
            return var1.substring(0, var1.length() - 4);
        }
    }

    List parse(String var1) throws AsException {
        String var2 = var1 + ".asm";
        ArrayList var5 = new ArrayList();
        int var6 = 1;

        try {
            BufferedReader var4 = new BufferedReader(new FileReader(var2));

            while (true) {
                Instruction var7;
                do {
                    String var3;
                    if ((var3 = var4.readLine()) == null) {
                        var4.close();
                        return var5;
                    }

                    var7 = new Instruction(var3, var6++);
                } while (var7.getOpcode() == null && var7.getLabel() == null);

                var5.add(var7);
            }
        } catch (IOException var8) {
            throw new AsException("Couldn't read file (" + var2 + ")");
        }
    }

    private List pass_zero(List var1) throws AsException {
        ArrayList var2 = new ArrayList();
        Iterator var3 = var1.iterator();

        while (var3.hasNext()) {
            Instruction var4 = (Instruction) var3.next();
            var4.splitLabels(var2);
        }

        return var2;
    }

    void pass_one(SymTab var1, List var2) throws AsException {
        int var3 = -1;
        Iterator var4 = var2.iterator();

        while (var4.hasNext()) {
            Instruction var5 = (Instruction) var4.next();
            if (var5.getLabel() != null) {
                if (var5.getLabel().length() > 20) {
                    var5.error("Labels can be no longer than 20 characters ('" + var5.getLabel()
                            + "').");
                }

                if (var3 > 65535) {
                    var5.error("Label cannot be represented in 16 bits (" + var3 + ")");
                }

                if (!var1.insert(var5.getLabel(), var3)) {
                    var5.error("Duplicate label ('" + var5.getLabel() + "')");
                }
            } else {
                var5.setAddress(var3);
                InstructionDef var6 = (InstructionDef) ISA.formatToDef.get(var5.getFormat());
                if (var6 == null) {
                    throw new AsException(var5, "Undefined opcode '" + var5.getOpcode() + "'");
                }

                var3 = var6.getNextAddress(var5);
            }
        }

    }

    void pass_two(SymTab var1, List var2, String var3) throws AsException {
        ArrayList var4 = new ArrayList();
        Iterator var5 = var2.iterator();

        while (var5.hasNext()) {
            Instruction var6 = (Instruction) var5.next();
            if (var6.getLabel() == null) {
                String var7 = var6.getOpcode();
                if (var7 == null) {
                    Console.println(var6.getOriginalLine());
                }

                InstructionDef var8 = (InstructionDef) ISA.formatToDef.get(var6.getFormat());
                if (var8 != null) {
                    var8.encode(var1, var6, var4);
                }
            }
        }

        String var10 = var3 + ".obj";

        try {
            BufferedOutputStream var11 = new BufferedOutputStream(new FileOutputStream(var10));
            Iterator var12 = var4.iterator();

            while (var12.hasNext()) {
                Word var13 = (Word) var12.next();
                var13.writeWordToFile(var11);
            }

            var11.close();
        } catch (IOException var9) {
            throw new AsException("Couldn't write file (" + var10 + ")");
        }
    }

    void gen_sym(SymTab var1, List var2, String var3) throws AsException {
        String var4 = var3 + ".sym";
        Enumeration var6 = var1.get_labels();

        try {
            BufferedWriter var5 = new BufferedWriter(new FileWriter(var4));
            var5.write("// Symbol table\n");
            var5.write("// Scope level 0:\n");
            var5.write("//\tSymbol Name       Page Address\n");
            var5.write("//\t----------------  ------------\n");

            while (var6.hasMoreElements()) {
                String var7 = (String) var6.nextElement();
                var5.write("//\t" + var7);

                int var8;
                for (var8 = 0; var8 < 16 - var7.length(); ++var8) {
                    var5.write(" ");
                }

                var8 = var1.lookup(var7);
                String var9 = this.formatAddress(var8);
                var5.write("  " + var9 + "\n");
            }

            Iterator var11 = var2.iterator();

            while (var11.hasNext()) {
                Instruction var12 = (Instruction) var11.next();
                if (var12.getOpcode() != null) {
                    InstructionDef var13 = (InstructionDef) ISA.formatToDef.get(var12.getFormat());
                    if (!var13.isDataDirective()) {
                        var5.write("//\t$               " + this.formatAddress(var12.getAddress())
                                + "\n");
                    }
                }
            }

            var5.newLine();
            var5.close();
        } catch (IOException var10) {
            throw new AsException("Couldn't write file (" + var4 + ")");
        }
    }

    private String formatAddress(int var1) {
        String var2 = "0000" + Integer.toHexString(var1).toUpperCase();
        return var2.substring(var2.length() - 4);
    }
}
