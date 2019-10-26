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

    String as(String[] asArgs) throws AsException {
        String arg = null;
        SymbolTable symTab = new SymbolTable();

        for (int i = 0; i < asArgs.length; ++i) {
            if (asArgs[i].length() == 0) {
                throw new AsException("Null arguments are not permitted.");
            }

            arg = asArgs[i];
        }

        if (arg != null && arg.length() != 0) {
            String filename = this.baseFilename(arg);
            List<Instruction> instructions = this.parse(filename);
            instructions = this.passZero(instructions);
            this.passOne(symTab, instructions);
            this.pass_two(symTab, instructions, filename);
            this.gen_sym(symTab, instructions, filename);
            return "";
        } else {
            throw new AsException("No .asm file specified.");
        }
    }

    String baseFilename(String var1) throws AsException {
        if (!var1.endsWith(".asm")) {
            throw new AsException("Input file must have .asm suffix ('" + var1 + "')");
        } else {
            return var1.substring(0, var1.length() - 4);
        }
    }

    private List<Instruction> parse(String baseFilename) throws AsException {
        String fullFilename = baseFilename + ".asm";
        ArrayList var5 = new ArrayList();
        int var6 = 1;

        try {
            BufferedReader var4 = new BufferedReader(new FileReader(fullFilename));

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
            throw new AsException("Couldn't read file (" + fullFilename + ")");
        }
    }

    private List passZero(List<Instruction> instructions) throws AsException {
        ArrayList<Instruction> newList = new ArrayList<>();

        for (Instruction instruction : instructions) {
            instruction.splitLabels(newList);
        }

        return newList;
    }

    private void passOne(SymbolTable symbolTable, List<Instruction> instructions) throws AsException {
        int address = -1;

        for (Instruction instruction : instructions) {
            if (instruction.getLabel() != null) {
                if (instruction.getLabel().length() > 20) {
                    instruction.error("Labels can be no longer than 20 characters ('" + instruction.getLabel() + "').");
                }

                if (address > 65535) {
                    instruction.error("Label cannot be represented in 16 bits (" + address + ")");
                }

                if (!symbolTable.insert(instruction.getLabel(), address)) {
                    instruction.error("Duplicate label ('" + instruction.getLabel() + "')");
                }
            } else {
                instruction.setAddress(address);
                InstructionDef instructionDef = ISA.formatToDef.get(instruction.getFormat());
                if (instructionDef == null) {
                    throw new AsException(instruction, "Undefined opcode '" + instruction.getOpcode() + "'");
                }

                address = instructionDef.getNextAddress(instruction);
            }
        }

    }

    void pass_two(SymbolTable var1, List var2, String var3) throws AsException {
        ArrayList var4 = new ArrayList();
        Iterator var5 = var2.iterator();

        while (var5.hasNext()) {
            Instruction var6 = (Instruction) var5.next();
            if (var6.getLabel() == null) {
                String var7 = var6.getOpcode();
                if (var7 == null) {
                    Console.println(var6.getOriginalLine());
                }

                InstructionDef var8 = ISA.formatToDef.get(var6.getFormat());
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

    void gen_sym(SymbolTable var1, List var2, String var3) throws AsException {
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
                    InstructionDef var13 = ISA.formatToDef.get(var12.getFormat());
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
