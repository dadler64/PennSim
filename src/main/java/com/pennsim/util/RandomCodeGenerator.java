package com.pennsim.util;

import com.pennsim.ISA;
import com.pennsim.InstructionDefinition;
import com.pennsim.LC3;
import com.pennsim.P37X;
import com.pennsim.Word;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

/**
 * Class which generates a specified amount of random lines of P37X instructions
 * which get compiled directly upon generation
 */
public class RandomCodeGenerator {

    private static final int MAX_BRANCH_DISTANCE = 4;
    private static Vector<Integer> memoryInstructionIndices;
    private static Vector<Integer> branchInstructionIndices;
    private static Vector<Integer> otherInstructionIndices;
    private static boolean isLC3 = false;
    private static boolean isP37X = false;
    private static int instructionsToGenerate;

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public static void main(String[] args) {
        if (args.length != 3) {
//            System.out.println("Usage: <number of instructions to generate>  <filename>");
            printUsage();
        } else {

//            for (int i = 0; i < args.length; ++i) {
//                if (Integer.parseInt(args[i]) != null) {
//                    GRAPHICAL_MODE = false;
//                } else if (args[i].equalsIgnoreCase("-s")) {
//                    ++i;
//                    if (i >= args.length) {
//                        System.out.println("Error: -s requires a script filename");
//                        return;
//                    }
//
//                    str = args[i];
//                } else if (args[i].equalsIgnoreCase("-lc3")) {
//                    LC3 = true;
//                } else if (args[i].equalsIgnoreCase("-p37x")) {
//                    P37X = true;
//                } else {
//                    if (!args[i].equalsIgnoreCase("-pipeline")) {
//                        System.out.println("Arg '" + args[i] + "' not recognized");
//                        printUsage();
//                        return;
//                    }
//
//                    PIPELINE_MODE = true;
//                }
//            }

            try {
                instructionsToGenerate = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number: " + args[0]);
                return;
            }

            if (args[1].equalsIgnoreCase("-lc3")) {
                isLC3 = true;
                isP37X = false;
            } else if (args[1].equalsIgnoreCase("-p37x")) {
                isLC3 = false;
                isP37X = true;
            } else {
                System.err.println("Failed to read instructions set.");
                printUsage();
                System.exit(0);
            }

            String baseFilename;
            if (args[2].endsWith(".obj")) {
                baseFilename = args[2].substring(0, args[1].length() - 5);
            } else {
                baseFilename = args[2];
            }

            String objectFilename = baseFilename + ".obj";
            String symbolFilename = baseFilename + ".sym";
            File objectFile = new File(objectFilename);

            BufferedOutputStream outputStream;
            try {
                if (!objectFile.createNewFile()) {
                    System.err.println("File " + objectFilename + " already exists.");
                    return;
                }

                outputStream = new BufferedOutputStream(new FileOutputStream(objectFile));
            } catch (IOException e) {
                System.err.println("Error opening file: " + objectFile.getName());
                return;
            }

            memoryInstructionIndices = new Vector<>();
            branchInstructionIndices = new Vector<>();
            otherInstructionIndices = new Vector<>();

            // Populate either P37X or LC3 Instructions
            if (isP37X && !isLC3) {
                System.out.println("Populating P37X Instructions");
                (new P37X()).init();
                populateValidP37XInstructionList();
            } else if (isLC3 && !isP37X) {
                System.err.println("LC3 has not been implemented yet!");
                System.exit(-2);
//                (new LC3()).init();
//                populateValidLC3InstructionList();
            } else {
                System.err.println("No ISA selected!");
                System.exit(-3);
            }

            int memoryInstructionSize;
            try {
                (new Word(512)).writeWordToFile(outputStream);
                Random random = new Random();
                memoryInstructionSize = memoryInstructionIndices.size();
                int branchInstructionSize = branchInstructionIndices.size();
                int otherInstructionSize = otherInstructionIndices.size();

                for (int i = 0; i < instructionsToGenerate; ++i) {
                    int num = random.nextInt(10);
                    int index = -1;
                    switch (num) {
                        case 0:
                            break;
                        case 1:
                            index = memoryInstructionIndices.elementAt(random.nextInt(memoryInstructionSize));
                            InstructionDefinition instructionDef = ISA.lookupTable[index];
                            if (!instructionDef.getOpcode().equalsIgnoreCase("LDR")) {
                                int pcOffset = instructionDef.getPCOffset(new Word(index));
                                if (pcOffset + i < 0) {
                                    index = branchInstructionIndices.elementAt(random.nextInt(branchInstructionSize));
                                }
                            }
                            break;
                        case 2:
                            break;
                        case 3:
                            index = branchInstructionIndices.elementAt(random.nextInt(branchInstructionSize));
                            break;
                        default:
                            index = otherInstructionIndices.elementAt(random.nextInt(otherInstructionSize));
                    }

//                    InstructionDefinition instructionDef = ISA.lookupTable[index];
                    if (index >= 0 && index <= 3) {
                        Word word = new Word(index);
                        word.writeWordToFile(outputStream);
                    }
                }

                outputStream.close();
            } catch (IOException e) {
                System.out.println("Error writing object file: " + e.toString());
            }

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(symbolFilename));
                writer.write("// Symbol table\n");
                writer.write("// Scope level 0:\n");
                writer.write("//\tSymbol Name       Page Address\n");
                writer.write("//\t----------------  ------------\n");

                for (memoryInstructionSize = 512; memoryInstructionSize < instructionsToGenerate + 512;
                        ++memoryInstructionSize) {
                    writer.write("//\t$               " + String.format("%04X", memoryInstructionSize) + "\n");
                }

                writer.newLine();
                writer.close();
            } catch (IOException e) {
                System.out.println("Error writing symbol table file: " + symbolFilename);
            }

        }
    }

    private static void printUsage() {
        System.out.println("Usage: <number of instructions to generate> [-lc3 | -p37x] <output filename>");
        System.out.println("\t-lc3 : simulate the LC-3 ISA");
        System.out.println("\t-p37x : simulate the P37X ISA");
    }

    private static void populateValidLC3InstructionList() {

    }

    private static void populateValidP37XInstructionList() {
        for (int i = 0; i < ISA.lookupTable.length; ++i) {
            InstructionDefinition instructionDef = ISA.lookupTable[i];
            Word word = new Word(i);
            if (instructionDef != null && !instructionDef.isDataDirective() && !instructionDef.getOpcode().equalsIgnoreCase("NOOP")) {
                if (!instructionDef.isBranch() && !instructionDef.getOpcode().equalsIgnoreCase("JSR") && !instructionDef
                        .getOpcode().equalsIgnoreCase("JUMP")) {
                    if (!instructionDef.isCall() && !instructionDef.getOpcode().equalsIgnoreCase("JUMPR") && !instructionDef
                            .getOpcode().equalsIgnoreCase("RTT") && !instructionDef.getOpcode()
                            .equalsIgnoreCase("LDR") && !instructionDef.getOpcode().equalsIgnoreCase("STR")) {
                        if (instructionDef.getOpcode().equalsIgnoreCase("LD")) {
                            memoryInstructionIndices.add(i);
                        } else if (instructionDef.getOpcode().equalsIgnoreCase("ST")) {
                            if (instructionDef.getPCOffset(word) < -1) {
                                memoryInstructionIndices.add(i);
                            }
                        } else {
                            otherInstructionIndices.add(i);
                        }
                    }
                } else if (instructionDef.getPCOffset(word) <= MAX_BRANCH_DISTANCE && instructionDef.getPCOffset(word) > 0) {
                    branchInstructionIndices.add(i);
                }
            }
        }

    }
}
