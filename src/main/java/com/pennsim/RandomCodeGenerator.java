package com.pennsim;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

public class RandomCodeGenerator {

    private static final int MAX_BRANCH_DISTANCE = 4;
    private static Vector memoryInsnIndices;
    private static Vector branchInsnIndices;
    private static Vector otherInsnIndices;
    private static int insnsToGenerate;

    public static void main(String[] var0) {
        if (var0.length != 2) {
            System.out.println("Usage: <number of insns to generate> <filename>");
        } else {
            insnsToGenerate = -1;

            try {
                insnsToGenerate = Integer.parseInt(var0[0]);
            } catch (NumberFormatException var16) {
                System.out.println("Invalid number: " + var0[0]);
                return;
            }

            String var2;
            if (var0[1].endsWith(".obj")) {
                var2 = var0[1].substring(0, var0[1].length() - 5);
            } else {
                var2 = var0[1];
            }

            String var3 = var2 + ".obj";
            String var4 = var2 + ".sym";
            File var5 = new File(var3);

            BufferedOutputStream var1;
            try {
                if (!var5.createNewFile()) {
                    System.out.println("File " + var3 + " already exists.");
                    return;
                }

                var1 = new BufferedOutputStream(new FileOutputStream(var5));
            } catch (IOException var19) {
                System.out.println("Error opening file: " + var5.getName());
                return;
            }

            memoryInsnIndices = new Vector();
            branchInsnIndices = new Vector();
            otherInsnIndices = new Vector();
            (new P37X()).init();
            populateValidP37XInsnList();

            int var7;
            try {
                (new Word(512)).writeWordToFile(var1);
                Random var6 = new Random();
                var7 = memoryInsnIndices.size();
                int var8 = branchInsnIndices.size();
                int var9 = otherInsnIndices.size();

                for (int var10 = 0; var10 < insnsToGenerate; ++var10) {
                    int var11 = var6.nextInt(10);
                    int var12;
                    switch (var11) {
                        case 0:
                        case 1:
                            var12 = (Integer) memoryInsnIndices.elementAt(var6.nextInt(var7));
                            InstructionDef var13 = ISA.lookupTable[var12];
                            if (!var13.getOpcode().equalsIgnoreCase("LDR")) {
                                int var15 = var13.getPCOffset(new Word(var12));
                                if (var15 + var10 < 0) {
                                    var12 = (Integer) branchInsnIndices
                                            .elementAt(var6.nextInt(var8));
                                }
                            }
                            break;
                        case 2:
                        case 3:
                            var12 = (Integer) branchInsnIndices.elementAt(var6.nextInt(var8));
                            break;
                        default:
                            var12 = (Integer) otherInsnIndices.elementAt(var6.nextInt(var9));
                    }

                    InstructionDef var10000 = ISA.lookupTable[var12];
                    Word var14 = new Word(var12);
                    var14.writeWordToFile(var1);
                }

                var1.close();
            } catch (IOException var18) {
                System.out.println("Error writing object file: " + var18.toString());
            }

            try {
                BufferedWriter var20 = new BufferedWriter(new FileWriter(var4));
                var20.write("// Symbol table\n");
                var20.write("// Scope level 0:\n");
                var20.write("//\tSymbol Name       Page Address\n");
                var20.write("//\t----------------  ------------\n");

                for (var7 = 512; var7 < insnsToGenerate + 512; ++var7) {
                    var20.write("//\t$               " + String.format("%04X", var7) + "\n");
                }

                var20.newLine();
                var20.close();
            } catch (IOException var17) {
                System.out.println("Error writing symbol table file: " + var4);
            }

        }
    }

    private static void populateValidP37XInsnList() {
        for (int var0 = 0; var0 < ISA.lookupTable.length; ++var0) {
            InstructionDef var1 = ISA.lookupTable[var0];
            Word var2 = new Word(var0);
            if (var1 != null && !var1.isDataDirective() && !var1.getOpcode()
                    .equalsIgnoreCase("NOOP")) {
                if (!var1.isBranch() && !var1.getOpcode().equalsIgnoreCase("JSR") && !var1
                        .getOpcode().equalsIgnoreCase("JUMP")) {
                    if (!var1.isCall() && !var1.getOpcode().equalsIgnoreCase("JUMPR") && !var1
                            .getOpcode().equalsIgnoreCase("RTT") && !var1.getOpcode()
                            .equalsIgnoreCase("LDR") && !var1.getOpcode().equalsIgnoreCase("STR")) {
                        if (var1.getOpcode().equalsIgnoreCase("LD")) {
                            memoryInsnIndices.add(new Integer(var0));
                        } else if (var1.getOpcode().equalsIgnoreCase("ST")) {
                            if (var1.getPCOffset(var2) < -1) {
                                memoryInsnIndices.add(new Integer(var0));
                            }
                        } else {
                            otherInsnIndices.add(new Integer(var0));
                        }
                    }
                } else if (var1.getPCOffset(var2) <= 4 && var1.getPCOffset(var2) > 0) {
                    branchInsnIndices.add(new Integer(var0));
                }
            }
        }

    }
}
