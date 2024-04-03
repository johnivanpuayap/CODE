package src.analyzer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import src.nodes.ASTNode;
import src.nodes.DeclarationNode;
import src.nodes.FunctionNode;
import src.nodes.ProgramNode;
import src.nodes.StringLiteralNode;
import src.nodes.VariableNode;

public class SemanticsAnalyzer {
    private ProgramNode program;
    private Set<String> reservedWords = new HashSet<>();
    private Set<String> declaredFunctions = new HashSet<>();

    public SemanticsAnalyzer(ProgramNode program) {
        this.program = program;
        initializeReservedWords();
        initializeDeclaredFunctions();
    }

    public void analyze() {
        List<DeclarationNode> declarations = program.getDeclarations();
        List<FunctionNode> functionCalls = program.getFunctionCalls();
        
        for (DeclarationNode declaration : declarations) {
            // Extract information from the DeclarationNode
            String dataType = declaration.getDataType();
            String variableName = declaration.getVariableName();
            String value = declaration.getValue();
            
            // Check if the variable name is a reserved word
            if (isReservedWord(variableName)) {
                error("Variable name cannot be a reserved word: " + variableName + " at line " + declaration.getPosition().getLine());
            }
            
            // Check for valid variable name format
            if (!isValidVariableName(variableName)) {
                error("Invalid variable name at line " + declaration.getPosition().getLine() + ": " + variableName);
            }
    
            // Check for valid value based on data type
            if (!isValidValue(dataType, value)) {
                error("Invalid value at line " + declaration.getPosition().getLine() + " for " + dataType + " variable " + variableName + ": " + value);
            }
            
            // Perform semantics analysis and execution logic for each declaration
            // For example, you might evaluate expressions, check variable scopes, etc.
        }

        for (FunctionNode function: functionCalls) {
            String functionName = function.getFunctionName();

            // Check if function has been declared
            if (!isFunctionDeclared(functionName)) {
                error("Function " + functionName + " has not been declared yet at line " + function.getPosition().getLine() + " position " + function.getPosition().getPosition());
            }

            if (functionName == "DISPLAY") {
                for (ASTNode argument : function.getArguments()) {
                    if (argument instanceof StringLiteralNode) {
                        StringLiteralNode stringLiteral = (StringLiteralNode) argument;
                        
                    } else if (argument instanceof VariableNode){
                        VariableNode variable = (VariableNode) argument;
                        boolean variableFound = false;
                        for (DeclarationNode declaredVariable : declarations) {
                            if (declaredVariable.getVariableName().equals(variable.getVariableName())) {
                                variableFound = true;
                            }
                        }
                        if (!variableFound) {
                            error("Variable " + variable.getVariableName() + " has not been declared yet at line " + variable.getPosition().getLine() + " position " + variable.getPosition().getPosition());
                        }
                    }
                }
            }
            
        }
    }
    
    private boolean isReservedWord(String variableName) {
        return reservedWords.contains(variableName);
    }

    private boolean isFunctionDeclared(String functionName) {
        return declaredFunctions.contains(functionName);
    }
    
    private boolean isValidVariableName(String variableName) {
        // Check if the variable name matches the pattern
        return variableName.matches("[a-z_][a-zA-Z0-9_]*");
    }
    
    private boolean isValidValue(String dataType, String value) {
        return value.isEmpty() ||
                (dataType.equals("INT") && value.matches("-?\\d+")) ||
                (dataType.equals("FLOAT") && value.matches("-?\\d*\\.?\\d+")) ||
                (dataType.equals("BOOL") && (value.matches("TRUE") || value.matches("FALSE"))) ||
                (dataType.equals("CHAR") && value.length() == 1);
    }


    // Method to handle errors
    private void error(String message) {
        throw new RuntimeException(message);
    }

    private void initializeDeclaredFunctions() {
        declaredFunctions.add("DISPLAY");
    }

    private void initializeReservedWords() {
        // Add all reserved words to the set
        reservedWords.add("BEGIN");
        reservedWords.add("CODE");
        reservedWords.add("END");
        reservedWords.add("INT");
        reservedWords.add("CHAR");
        reservedWords.add("BOOL");
        reservedWords.add("FLOAT");
        reservedWords.add("DISPLAY");
        reservedWords.add("SCAN");
        reservedWords.add("IF");
        reservedWords.add("ELSE");
        reservedWords.add("WHILE");
        reservedWords.add("TRUE");
        reservedWords.add("FALSE");
    }
}