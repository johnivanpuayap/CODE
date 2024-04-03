package src.analyzer;
import java.beans.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import src.nodes.DeclarationNode;
import src.nodes.ExpressionNode;
import src.nodes.ProgramNode;
import src.nodes.StatementNode;
import src.utils.Token;

public class SemanticsAnalyzer {
    private ProgramNode program;
    private Set<String> reservedWords = new HashSet<>();

    public SemanticsAnalyzer(ProgramNode program) {
        this.program = program;
        initializeReservedWords();
    }

    public void analyze() {
        List<DeclarationNode> declarations = program.getDeclarations();
        List<StatementNode> statements = program.getStatements();
        
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

        for (StatementNode statement : statements) {
            // Start by evaluating expression

        }
    }
    
    private boolean isReservedWord(String variableName) {
        return reservedWords.contains(variableName);
    }
    
    private boolean isValidVariableName(String variableName) {
        // Check if the variable name matches the pattern
        return variableName.matches("[a-z_][a-zA-Z0-9_]*");
    }
    
    private boolean isValidValue(String dataType, String value) {
        if (value.isEmpty()) {
            return true;
        }
        
        switch(dataType) {
            case "INT":
                try {
                    Integer.parseInt(value);
                    return value.matches("-?\\d+"); // The value fits within 4 bytes
                } catch (NumberFormatException e) {
                    if (e.getMessage().contains("out of range")) {
                        error("Invalid value for INT data type. The number is too large to fit in 4 bytes: " + value);
                    } else {
                        error("Invalid value for INT data type. Expected an integer value, but got: " + value);
                    }
                }
            case "FLOAT":
                try {
                    Float.parseFloat(value);
                    return value.matches("-?\\d+(\\.\\d+)?");
                } catch (NumberFormatException e) {
                    if (e.getMessage().contains("out of range")) {
                        error("Invalid value for FLOAT data type. The number is too large: " + value);
                    } else {
                        error("Invalid value for FLOAT data type. Expected a floating-point value, but got: " + value);
                    }
                }
                
            case "CHAR":
                if (value.matches("'.'")) {
                    return true;
                } else {
                    error("Invalid value for CHAR data type. Expected a single character enclosed in single quotes, but got: " + value);
                }
            case "BOOL":
                if (value.equals("TRUE") || value.equals("FALSE")) {
                    return true;
                } else {
                    error("Invalid value for BOOL data type. Expected \"TRUE\" or \"FALSE\" (case sensitive), but got: " + value);
                }
            default:
                return false;
        }
    }

    // Method to handle errors
    private void error(String message) {
        throw new RuntimeException(message);
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

    private boolean isArithmeticExpression(ExpressionNode expression) {
    for (Token token : expression.getTokens()) {
        if (token.getType() == Token.Type.OPERATOR && "+-*/".contains(token.getValue())) {
            return true;
        }
    }
        return false;
    }
}