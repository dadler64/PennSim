package com.pennsim;

import com.pennsim.exception.AsException;
import com.pennsim.gui.Console;
import com.pennsim.gui.EditorTab;
import com.pennsim.isa.ISA;
import com.pennsim.isa.Instruction;
import com.pennsim.isa.InstructionDefinition;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class Assembler {

    public String assembleFile(String[] asArgs) throws AsException {
        String arg = null;
        SymbolTable symTab = new SymbolTable();

        for (String asArg : asArgs) {
            if (asArg.length() == 0) {
                throw new AsException("Null arguments are not permitted.");
            }

            arg = asArg;
        }

        if (arg != null) {
            String filename = this.baseFilename(arg);
            List<Instruction> instructions = this.parseFile(filename);
            instructions = this.passZero(instructions);
            this.passOne(symTab, instructions);
            this.passTwo(symTab, instructions, filename);
            this.generateSymbolFile(symTab, instructions, filename);
            return "";
        } else {
            throw new AsException("No .asm file specified.");
        }
    }

    public String assembleTab(EditorTab tab, boolean warnFlag) throws AsException {
        SymbolTable symTab = new SymbolTable();

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(tab.getText().split("\n")));

        String filename = tab.getFilename();
        List<Instruction> instructions = this.parseTab(lines);
        instructions = this.passZero(instructions);
        this.passOne(symTab, instructions);
        this.passTwo(symTab, instructions, filename);
        this.generateSymbolFile(symTab, instructions, filename);
        return "";
    }

    private String baseFilename(String filename) throws AsException {
        if (!filename.endsWith(".asm")) {
            throw new AsException("Input file must have .asm suffix ('" + filename + "')");
        } else {
            return filename.substring(0, filename.length() - 4);
        }
    }

    /**
     * Get the instructions from the
     * @param baseFilename
     * @return
     * @throws AsException
     */
    private List<Instruction> parseFile(String baseFilename) throws AsException {
        String asmFilename = baseFilename + ".asm";
        ArrayList<Instruction> instructions = new ArrayList<>();
        int lineNumber = 1;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(asmFilename));

            while (true) {
                Instruction instruction;
                do {
                    String line;
                    if ((line = reader.readLine()) == null) {
                        reader.close();
                        return instructions;
                    }

                    instruction = new Instruction(line, lineNumber++);
                } while (instruction.getOpcode() == null && instruction.getLabel() == null);

                instructions.add(instruction);
            }
        } catch (IOException e) {
            throw new AsException("Couldn't read file (" + asmFilename + ")");
        }
    }

    /**
     *
     * @return
     * @throws AsException
     */
    private List<Instruction> parseTab(List<String> lines) throws AsException {
        ArrayList<Instruction> instructions = new ArrayList<>();
        int lineNumber = 1;
        Instruction instruction = null;

        for (String line: lines) {
            instruction = new Instruction(line, lineNumber++);

            if (instruction.getOpcode() != null && instruction.getLabel() != null) {
                instructions.add(instruction);
            }
        }

        return instructions;
    }

    /**
     *
     * @param instructions
     * @return
     * @throws AsException
     */
    private List<Instruction> passZero(List<Instruction> instructions) throws AsException {
        ArrayList<Instruction> newList = new ArrayList<>();

        for (Instruction instruction : instructions) {
            instruction.splitLabels(newList);
        }

        return newList;
    }

    /**
     *
     * @param symbolTable
     * @param instructions
     * @throws AsException
     */
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
                InstructionDefinition instructionDef = ISA.formatToDefinition.get(instruction.getFormat());
                if (instructionDef == null) {
                    throw new AsException(instruction, "Undefined opcode '" + instruction.getOpcode() + "'");
                }

                address = instructionDef.getNextAddress(instruction);
            }
        }

    }

    /**
     *
     * @param symbolTable
     * @param Instructions
     * @param baseFilename
     * @throws AsException
     */
    private void passTwo(SymbolTable symbolTable, List<Instruction> Instructions, String baseFilename) throws AsException {
        ArrayList<Word> words = new ArrayList<>();

        for (Instruction instruction: Instructions) {
            if (instruction.getLabel() == null) {
                String opcode = instruction.getOpcode();
                if (opcode == null) {
                    Console.println(instruction.getOriginalLine());
                }

                InstructionDefinition definition = ISA.formatToDefinition.get(instruction.getFormat());
                if (definition != null) {
                    definition.encode(symbolTable, instruction, words);
                }
            }
        }

        String objectFilename = baseFilename + ".obj";

        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(objectFilename));

            for (Word word : words) {
                word.writeWordToFile(bufferedOutputStream);
            }

            bufferedOutputStream.close();
        } catch (IOException e) {
            throw new AsException("Couldn't write file (" + objectFilename + ")");
        }
    }

    private void generateSymbolFile(SymbolTable symbolTable, List<Instruction> instructions, String baseFilename) throws AsException {
        String symbolFilename = baseFilename + ".sym";
        Enumeration enumeration = symbolTable.getLabels();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(symbolFilename));
            writer.write("// Symbol table\n");
            writer.write("// Scope level 0:\n");
            writer.write("//\tSymbol Name       Page Address\n");
            writer.write("//\t----------------  ------------\n");

            while (enumeration.hasMoreElements()) {
                String label = (String) enumeration.nextElement();
                writer.write("//\t" + label);

                int index;
                for (index = 0; index < 16 - label.length(); ++index) {
                    writer.write(" ");
                }

                index = symbolTable.lookup(label);
                String address = this.formatAddress(index);
                writer.write("  " + address + "\n");
            }

            for (Instruction instruction : instructions) {
                if (instruction.getOpcode() != null) {
                    InstructionDefinition definition = ISA.formatToDefinition.get(instruction.getFormat());
                    if (!definition.isDataDirective()) {
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

    private String formatAddress(int address) {
        String formattedAddress = "0000" + Integer.toHexString(address).toUpperCase();
        return formattedAddress.substring(formattedAddress.length() - 4);
    }

}
