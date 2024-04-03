package src.parser;
import java.util.ArrayList;
import java.util.List;
import src.utils.Token;
import src.utils.Position;
import src.nodes.ASTNode;
import src.nodes.ProgramNode;
import src.nodes.DeclarationNode;
import src.nodes.FunctionNode;
import src.nodes.VariableNode;
import src.nodes.StringLiteralNode;
import src.nodes.SpecialCharacterNode;

// Syntax Analyzer or Parser class to generate Abstract Syntax Tree (AST) from tokens
public class Parser {
    private List<Token> tokens;
    private int currentTokenIndex;
    private List<DeclarationNode> declarations;
    private List<FunctionNode> functionCalls;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.declarations = new ArrayList<>();
        this.functionCalls = new ArrayList<>();
    }

    public ProgramNode parse() {
        
        if(!match(Token.Type.BEGIN_CODE)) {
            error("Expected BEGIN CODE");
        }

        // Parse the content between BEGIN CODE and END CODE
        parseCodeBlock();

        if (!match(Token.Type.END_CODE)) {
            error("Expected END CODE");
        }

        // You change return the statements later if needed
        return new ProgramNode(declarations, null, functionCalls);
    }

    private void parseCodeBlock() {
        while (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() != Token.Type.END_CODE) {
            
            Token currentToken = tokens.get(currentTokenIndex);

            if (currentToken.getType() == Token.Type.DATA_TYPE) {
                // System.out.println("Data Type: " + currentToken.getValue());
                DeclarationNode declaration = parseVariableDeclaration();
                declarations.add(declaration);
            }

            if (currentToken.getType() == Token.Type.FUNCTION) {
                // System.out.println("Function Call: " + currentToken.getValue());
                FunctionNode function = parseFunctionCall();
                functionCalls.add(function);
                currentTokenIndex--;
            }

            currentTokenIndex++;
        }
    }

    private boolean match(Token.Type type) {
        // System.out.println("Current Token: " + tokens.get(currentTokenIndex));
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() == type) {
            return true;
        }
        return false;
    }

    private void error(String message) {
        throw new RuntimeException(message);
    }

    private DeclarationNode parseVariableDeclaration() {
        
        Position variablePosition = tokens.get(currentTokenIndex + 1).getPosition();
        String dataType = tokens.get(currentTokenIndex).getValue();

        // CHAR and BOOL variable declaration require DELIMITERS / quotation marks
        if (dataType == "CHAR" || dataType == "BOOL") {
            // Ensure that there are enough tokens to represent a CHAR variable declaration
            if (currentTokenIndex + 5 >= tokens.size()) {
                error("Invalid variable declaration");
            }
            
            if (tokens.get(currentTokenIndex + 1).getType() == Token.Type.VARIABLE &&
                ((tokens.get(currentTokenIndex + 2).getType() == Token.Type.ASSIGNMENT &&
                tokens.get(currentTokenIndex + 3).getType() == Token.Type.DELIMITER &&
                tokens.get(currentTokenIndex + 4).getType() == Token.Type.VALUE &&
                tokens.get(currentTokenIndex + 5).getType() == Token.Type.DELIMITER) ||
                tokens.get(currentTokenIndex + 2).getType() != Token.Type.ASSIGNMENT)) {
    
                // Parse variable declaration with or without initialization
                String variableName = tokens.get(currentTokenIndex + 1).getValue();
                String value = "";
        
                if (tokens.get(currentTokenIndex + 2).getType() == Token.Type.ASSIGNMENT) {
                    // Checks open single quotation
                    if (tokens.get(currentTokenIndex + 3).getType() != Token.Type.DELIMITER) {
                        error("CHAR declaration requires single quotes (\'\') at line " + tokens.get(currentTokenIndex + 3).getPosition().getPosition() + " position " + tokens.get(currentTokenIndex + 3).getPosition().getLine());
                    }
                    // Variable with initialization
                    value = tokens.get(currentTokenIndex + 4).getValue();
                    
                    // Checks closed single quotation
                    if (tokens.get(currentTokenIndex + 5).getType() != Token.Type.DELIMITER) {
                        error("CHAR declaration requires closing single quotes (\'\') at line " + tokens.get(currentTokenIndex + 5).getPosition().getPosition() + " position " + tokens.get(currentTokenIndex + 5).getPosition().getLine());
                    }
                    currentTokenIndex += 5; // Move to the token after the initialization value
                } else {
                    // Variable without initialization
                    currentTokenIndex += 1; // Move to the next token
                }

                // Output or process the parsed variable declaration as needed
                // System.out.println("Variable Declaration: " + dataType + " " + variableName + (value.isEmpty() ? "" : " = " + value));
        
                return new DeclarationNode(dataType, variableName, value, variablePosition);
            } else {
                error("Invalid variable declaration at line " + tokens.get(currentTokenIndex).getPosition().getPosition() + " position " + tokens.get(currentTokenIndex).getPosition().getLine());
            }
        }

        // Variable Declaration involving no delimiters / quotation marks
        // Ensure that there are enough tokens to represent a variable declaration
        if (currentTokenIndex + 2 >= tokens.size()) {
            error("Invalid variable declaration");
            return null; // Or handle the error appropriately
        }

        // Check token sequence for variable declaration
        if (tokens.get(currentTokenIndex + 1).getType() == Token.Type.VARIABLE &&
                ((tokens.get(currentTokenIndex + 2).getType() == Token.Type.ASSIGNMENT &&
                tokens.get(currentTokenIndex + 3).getType() == Token.Type.VALUE) ||
                tokens.get(currentTokenIndex + 2).getType() != Token.Type.ASSIGNMENT)) {
    
            // Parse variable declaration with or without initialization
            String variableName = tokens.get(currentTokenIndex + 1).getValue();
            String value = "";
    
            if (tokens.get(currentTokenIndex + 2).getType() == Token.Type.ASSIGNMENT) {
                // Variable with initialization
                value = tokens.get(currentTokenIndex + 3).getValue();
                currentTokenIndex += 3; // Move to the token after the initialization value
            } else {
                // Variable without initialization
                currentTokenIndex += 1; // Move to the next token
            }

            // Output or process the parsed variable declaration as needed
            // System.out.println("Variable Declaration: " + dataType + " " + variableName + (value.isEmpty() ? "" : " = " + value));
    
            return new DeclarationNode(dataType, variableName, value, variablePosition);
    
        } else {
            error("Invalid variable declaration at line " + tokens.get(currentTokenIndex).getPosition().getPosition() + " position " + tokens.get(currentTokenIndex).getPosition().getLine());
        }
        return null;
    }

    private FunctionNode parseFunctionCall() {
        String functionName = tokens.get(currentTokenIndex).getValue();
        List<ASTNode> arguments = new ArrayList<>();
        Position currentFunctionPosition = tokens.get(currentTokenIndex).getPosition();
        currentTokenIndex++;
        
        if (tokens.get(currentTokenIndex).getType() != Token.Type.COLON) {
            error("Missing colon (:) after DISPLAY call at line " + tokens.get(currentTokenIndex).getPosition().getLine() + " position " + tokens.get(currentTokenIndex).getPosition().getPosition());
        }

        if (functionName == "DISPLAY") {
            boolean start = true;

            while (currentTokenIndex < (tokens.size() - 1) && (
                    tokens.get(currentTokenIndex).getType() == Token.Type.COLON ||
                    tokens.get(currentTokenIndex).getType() == Token.Type.DELIMITER ||
                    tokens.get(currentTokenIndex).getType() == Token.Type.CONCATENATION ||
                    tokens.get(currentTokenIndex).getType() == Token.Type.SPECIAL_CHARACTER ||
                    tokens.get(currentTokenIndex).getType() == Token.Type.DISPLAY_VARIABLE)) {

                if (tokens.get(currentTokenIndex).getType() == Token.Type.COLON) {
                    start = false;
                    currentTokenIndex++;
                    continue;
                }

                if (tokens.get(currentTokenIndex).getType() == Token.Type.CONCATENATION) {
                    if (start) {
                        error("Cannot concatenate without any prior string literals or variables");
                    }

                    if (tokens.get(currentTokenIndex + 1).getType() != Token.Type.DELIMITER &&
                        tokens.get(currentTokenIndex + 1).getType() != Token.Type.DISPLAY_VARIABLE &&
                        tokens.get(currentTokenIndex + 1).getType() != Token.Type.SPECIAL_CHARACTER) {
                            error("Missing string literal/variable/special character in display concatenation");
                    }
                    currentTokenIndex++;
                }

                if (tokens.get(currentTokenIndex).getType() == Token.Type.SPECIAL_CHARACTER) {
                    Token token = tokens.get(currentTokenIndex);

                    if (tokens.get(currentTokenIndex).getValue() == "$") {
                        SpecialCharacterNode specialCharacter = new SpecialCharacterNode(token.getValue(), token.getPosition());
                        arguments.add(specialCharacter);
                        currentTokenIndex++;
                        continue;
                    }

                    if (tokens.get(currentTokenIndex).getValue() == "[") {
                        if(tokens.get(currentTokenIndex + 1).getType() == Token.Type.VALUE &&
                        tokens.get(currentTokenIndex + 2).getType() == Token.Type.SPECIAL_CHARACTER) {
                            SpecialCharacterNode specialCharacter = new SpecialCharacterNode(tokens.get(currentTokenIndex + 1).getValue(), tokens.get(currentTokenIndex + 1).getPosition());
                            arguments.add(specialCharacter);
                            currentTokenIndex += 3;
                            continue;
                        }
                    }
                }

                if (tokens.get(currentTokenIndex).getType() == Token.Type.DELIMITER) {
                    if (tokens.get(currentTokenIndex + 1).getType() == Token.Type.STRING_LITERAL &&
                        tokens.get(currentTokenIndex + 2).getType() == Token.Type.DELIMITER) {
                            String value = tokens.get(currentTokenIndex + 1).getValue();
                            StringLiteralNode newNode = new StringLiteralNode(value, tokens.get(currentTokenIndex).getPosition());
                            arguments.add(newNode);
                            currentTokenIndex += 3;
                    } else {
                        error("Missing delimiter in string literal");
                    }
                    continue;
                }

                if (tokens.get(currentTokenIndex).getType() == Token.Type.DISPLAY_VARIABLE) {
                    String variableName = tokens.get(currentTokenIndex).getValue();
                    VariableNode newNode  = new VariableNode(variableName, tokens.get(currentTokenIndex).getPosition());
                    arguments.add(newNode);
                    currentTokenIndex++;
                    continue;
                }
                
                // System.out.println("Found arguments: " + arguments.size());
            }
        }
        
        return new FunctionNode(functionName, arguments, currentFunctionPosition);
    }
}