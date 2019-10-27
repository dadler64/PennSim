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
import java.util.List;

class Assembler {    
    String as(String[] asArgs) throws AsException {
        String arg = null;
        SymbolTable symbolTable = new SymbolTable();

        for (String asArg : asArgs) {
            if (asArg.length() == 0) {
                throw new AsException("Null arguments are not permitted.");
            }

            arg = asArg;
        }

        if (arg != null && arg.length() != 0) {
            String filename = this.baseFilename(arg);
            List<Instruction> instructions = this.parse(filename);
            instructions = this.passZero(instructions);
            this.passOne(symbolTable, instructions);
            this.passTwo(symbolTable, instructions, filename);
            this.generateSymbols(symbolTable, instructions, filename);
            return "";
        } else {
            throw new AsException("No .asm file specified.");
        }
    }

    private String baseFilename(String filename) throws AsException {
        if (!filename.endsWith(".asm")) {
            throw new AsException("Input file must have .asm suffix ('" + filename + "')");
        } else {
            return filename.substring(0, filename.length() - 4);
        }
    }

    private List<Instruction> parse(String baseFilename) throws AsException {
        String assemblyFilename = baseFilename + ".asm";
        ArrayList<Instruction> instructionList = new ArrayList<>();
        int lineNumber = 1;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(assemblyFilename));

            while (true) {
                Instruction instruction;
                do {
                    String line;
                    if ((line = reader.readLine()) == null) {
                        reader.close();
                        return instructionList;
                    }

                    instruction = new Instruction(line, lineNumber++);
                } while (instruction.getOpcode() == null && instruction.getLabel() == null);

                instructionList.add(instruction);
            }
        } catch (IOException e) {
            throw new AsException("Couldn't read file (" + assemblyFilename + ")");
        }
    }

    private List<Instruction> passZero(List<Instruction> instructions) throws AsException {
        ArrayList<Instruction> instructionList = new ArrayList<>();

        for (Instruction instruction : instructions) {
            instruction.splitLabels(instructionList);
        }

        return instructionList;
    }

    private void passOne(SymbolTable symbolTable, List<Instruction> instructions) throws AsException {
        int address = -1;

        for (Instruction instruction : instructions) {
            if (instruction.getLabel() != null) {
                if (instruction.getLabel().length() > 20) {
                    instruction.error("Labels can be no longer than 20 characters ('" + instruction.getLabel() + "') at line: " + instruction.getLineNumber());
                }

                if (address > 65535) {
                    instruction.error("Label cannot be represented in 16 bits (" + address + ") at line: " + instruction.getLineNumber());
                }

                if (!symbolTable.insert(instruction.getLabel(), address)) {
                    instruction.error("Duplicate label ('" + instruction.getLabel() + "') at line: " + instruction.getLineNumber());
                }
            } else {
                instruction.setAddress(address);
                InstructionDef instructionDef = ISA.formatToDef.get(instruction.getFormat());
                if (instructionDef == null) {
                    throw new AsException(instruction, "Undefined opcode '" + instruction.getOpcode() + "' at line: " + instruction.getLineNumber());
                }

                address = instructionDef.getNextAddress(instruction);
            }
        }

    }

    private void passTwo(SymbolTable symbolTable, List<Instruction> instructions, String baseFilename) throws AsException {
        ArrayList<Word> words = new ArrayList<>();

        for (Instruction instruction : instructions) {
            if (instruction.getLabel() == null) {
                String opcode = instruction.getOpcode();
                if (opcode == null) {
                    Console.println(instruction.getOriginalLine());
                }

                InstructionDef instructionDef = ISA.formatToDef.get(instruction.getFormat());
                if (instructionDef != null) {
                    instructionDef.encode(symbolTable, instruction, words);
                }
            }
        }

        String filename = baseFilename + ".obj";

        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filename));

            for (Word word : words) {
                word.writeWordToFile(outputStream);
            }

            outputStream.close();
        } catch (IOException e) {
            throw new AsException("Couldn't write file (" + filename + ")");
        }
    }

    private void generateSymbols(SymbolTable symbolTable, List<Instruction> instructions, String baseFilename) throws AsException {
        String symbolFilename = baseFilename + ".sym";
        Enumeration enumeration = symbolTable.getSymbols();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(symbolFilename));
            writer.write("// Symbol table\n");
            writer.write("// Scope level 0:\n");
            writer.write("//\tSymbol Name       Page Address\n");
            writer.write("//\t----------------  ------------\n");

            while (enumeration.hasMoreElements()) {
                String symbol = (String) enumeration.nextElement();
                writer.write("//\t" + symbol);

                int index;
                for (index = 0; index < 16 - symbol.length(); ++index) {
                    writer.write(" ");
                }

                index = symbolTable.lookup(symbol);
                String address = this.formatAddress(index);
                writer.write("  " + address + "\n");
            }

            for (Instruction instruction : instructions) {
                if (instruction.getOpcode() != null) {
                    InstructionDef instructionDef = ISA.formatToDef.get(instruction.getFormat());
                    if (!instructionDef.isDataDirective()) {
                        writer.write("//\t$               " + this.formatAddress(instruction.getAddress()) + "\n");
                    }
                }
            }

            writer.newLine();
            writer.close();
        } catch (IOException e) {
            throw new AsException("Couldn't write file (" + symbolFilename + ")");
        }
    }

    private String formatAddress(int inputAddress) {
        String address = "0000" + Integer.toHexString(inputAddress).toUpperCase();
        return address.substring(address.length() - 4);
    }
}
