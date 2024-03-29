import java.util.List;
import java.util.ArrayList;

// Syntax Analyzer or Parser class to generate Abstract Syntax Tree (AST) from tokens

class Parser {
    private List<Token> tokens;
    private int currentTokenIndex;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }

    public ProgramNode parse() {
        List<DeclarationNode> declarations = new ArrayList<>();

        if(!match(Token.Type.BEGIN_CODE)) {
            error("Expected BEGIN CODE");
        }

        // Parse the content between BEGIN CODE and END CODE
        parseCodeBlock(declarations);

        if (!match(Token.Type.END_CODE)) {
            error("Expected END CODE");
        }

        // You change return the statements later if needed
        return new ProgramNode(declarations, null);
    }

    private void parseCodeBlock(List<DeclarationNode> declarations) {
        while (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() != Token.Type.END_CODE) {
            
            Token currentToken = tokens.get(currentTokenIndex);

            if (currentToken.getType() == Token.Type.DATA_TYPE) {
                System.out.println("Data Type: " + currentToken.getValue());
                DeclarationNode declaration = parseVariableDeclaration();
                declarations.add(declaration);
            }

            currentTokenIndex++;
        }
    }

    private boolean match(Token.Type type) {
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() == type) {
            currentTokenIndex++;
            return true;
        }
        return false;
    }

    private void error(String message) {
        throw new RuntimeException(message);
    }

    private DeclarationNode parseVariableDeclaration() {
        // Ensure that there are enough tokens to represent a variable declaration
        if (currentTokenIndex + 2 >= tokens.size()) {
            error("Invalid variable declaration");
            return null; // Or handle the error appropriately
        }
    
        // Check token sequence for variable declaration
        if (tokens.get(currentTokenIndex + 1).getType() == Token.Type.VARIABLE &&
                (tokens.get(currentTokenIndex + 2).getType() == Token.Type.ASSIGNMENT &&
                tokens.get(currentTokenIndex + 3).getType() == Token.Type.VALUE ||
                tokens.get(currentTokenIndex + 2).getType() != Token.Type.ASSIGNMENT)) {
    
            // Parse variable declaration with or without initialization
            String dataType = tokens.get(currentTokenIndex).getValue();
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
            System.out.println("Variable Declaration: " + dataType + " " + variableName + (value.isEmpty() ? "" : " = " + value));
    
            return new DeclarationNode(dataType, variableName, value, tokens.get(currentTokenIndex).getPosition());
    
        } else {
            error("Invalid variable declaration");
            return null;
        }
    }
}