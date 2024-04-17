//package src.parser;
//import java.util.ArrayList;
//import java.util.List;
//import src.utils.Token;
//import src.utils.Position;
//import src.nodes.ASTNode;
//import src.nodes.ProgramNode;
//import src.nodes.SpecialCharacterNode;
//import src.nodes.DeclarationNode;
//import src.nodes.StatementNode;
//import src.nodes.StringLiteralNode;
//import src.nodes.VariableNode;
//import src.nodes.ExpressionNode;
//import src.nodes.FunctionNode;
//
//// Syntax Analyzer or Parser class to generate Abstract Syntax Tree (AST) from tokens
//public class Parser_old {
//    private List<Token> tokens;
//    private int currentTokenIndex;
//    private List<FunctionNode> functions = new ArrayList<>();
//    private List<DeclarationNode> declarations = new ArrayList<>();
//    private List<StatementNode> statements = new ArrayList<>();
//
//    public Parser_old(List<Token> tokens) {
//        this.tokens = tokens;
//        this.currentTokenIndex = 0;
//    }
//
//    public ProgramNode parse() {
//        if(!match(Token.Type.BEGIN_CODE)) {
//            error("Expected BEGIN CODE");
//        }
//
//        // Parse the content between BEGIN CODE and END CODE
//        parseCodeBlock(declarations, statements);
//
//        if (!match(Token.Type.END_CODE)) {
//            error("Expected END CODE");
//        }
//
//        if (currentTokenIndex < tokens.size()) {
//
//            error("Unexpected tokens after END CODE");
//
//        }
//
//        return new ProgramNode(declarations, statements, functions);
//    }
//
//    private void parseCodeBlock(List<DeclarationNode> declarations, List<StatementNode> statements) {
//        while (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() != Token.Type.END_CODE) {
//
//            Token currentToken = tokens.get(currentTokenIndex);
//
//            if (currentToken.getType() == Token.Type.DATA_TYPE) {
//                System.out.println("Data Type: " + currentToken.getValue());
//                DeclarationNode declaration = parseVariableDeclaration();
//                declarations.add(declaration);
//            } else if(currentToken.getType() == Token.Type.VARIABLE) {
//
//                if(tokens.get(currentTokenIndex + 1).getType() == Token.Type.ASSIGNMENT && tokens.get(currentTokenIndex + 3).getType() == Token.Type.OPERATOR){
//                    StatementNode statement = parseArithmeticStatement();
//                    statements.add(statement);
//                }
//                else if(tokens.get(currentTokenIndex + 1).getType() == Token.Type.ASSIGNMENT && tokens.get(currentTokenIndex + 2).getType() == Token.Type.PARENTHESES){
//                    StatementNode statement = parseArithmeticStatement();
//                    statements.add(statement);
//                }
//                else {
//                    StatementNode statement = parseAssignmentStatement();
//                    statements.add(statement);
//                }
//            }
//
//            if (currentToken.getType() == Token.Type.FUNCTION) {
//                // System.out.println("Function Call: " + currentToken.getValue());
//                FunctionNode function = parseFunctionCall();
//                functions.add(function);
//                currentTokenIndex--;
//            }
//
//            currentTokenIndex++;
//        }
//    }
//
//    private boolean match(Token.Type type) {
//        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() == type) {
//            currentTokenIndex++;
//            return true;
//        }
//        return false;
//    }
//
//    private RuntimeException error(String message) {
//        throw new RuntimeException(message);
//    }
//
//    private RuntimeException error(String messsage, Token token) {
//        System.out.println("Error at " + token.getPosition() + " " + token.getValue());
//        throw new RuntimeException(messsage);
//    }
//
//    private DeclarationNode parseVariableDeclaration() {
//        // Ensure that there are enough tokens to represent a variable declaration
//        if (currentTokenIndex + 2 >= tokens.size()) {
//            error("Invalid variable declaration");
//            return null; // Or handle the error appropriately
//        }
//
//        // Check token sequence for variable declaration
//        if (tokens.get(currentTokenIndex + 1).getType() == Token.Type.VARIABLE &&
//                (tokens.get(currentTokenIndex + 2).getType() == Token.Type.ASSIGNMENT &&
//                tokens.get(currentTokenIndex + 3).getType() == Token.Type.VALUE ||
//                tokens.get(currentTokenIndex + 2).getType() != Token.Type.ASSIGNMENT)) {
//
//            // Parse variable declaration with or without initialization
//            String dataType = tokens.get(currentTokenIndex).getValue();
//            String variableName = tokens.get(currentTokenIndex + 1).getValue();
//            String value = null;
//
//            if (tokens.get(currentTokenIndex + 2).getType() == Token.Type.ASSIGNMENT) {
//                // Variable with initialization
//                value = tokens.get(currentTokenIndex + 3).getValue();
//                currentTokenIndex += 3; // Move to the token after the initialization value
//            } else {
//                // Variable without initialization
//                currentTokenIndex += 1; // Move to the next token
//            }
//
//            // Output or process the parsed variable declaration as needed
//            System.out.println("Variable Declaration: " + dataType + " " + variableName + (value == null ? "" : " = " + value));
//
//            return new DeclarationNode(dataType, variableName, value, tokens.get(currentTokenIndex).getPosition());
//
//        } else {
//            error("Invalid variable declaration at" + tokens.get(currentTokenIndex).getPosition());
//            return null;
//        }
//    }
//
//    private StatementNode parseAssignmentStatement() {
//        // Ensure that there are enough tokens to represent an assignment statement
//        if (currentTokenIndex + 2 >= tokens.size()) {
//            error("Invalid assignment statement at " + tokens.get(currentTokenIndex).getPosition());
//            return null; // Or handle the error appropriately
//        }
//
//        // Check token sequence for assignment statement
//
//        if (tokens.get(currentTokenIndex + 1).getType() == Token.Type.ASSIGNMENT &&
//                (tokens.get(currentTokenIndex + 2).getType() == Token.Type.VALUE || tokens.get(currentTokenIndex + 2).getType() == Token.Type.VARIABLE)){
//
//            // Parse assignment statement
//            Token leftSide = tokens.get(currentTokenIndex);
//            Token rightSide = tokens.get(currentTokenIndex + 2);
//
//
//            // Output or process the parsed assignment statement as needed
//            System.out.println("Assignment Statement: " + leftSide.getValue() + " = " + rightSide.getValue());
//
//            currentTokenIndex += 2; // Move to the token after the value
//
//            return new StatementNode(leftSide, rightSide);
//
//        } else {
//            error("Invalid assignment statement at " + tokens.get(currentTokenIndex).getPosition());
//            return null;
//        }
//    }
//
//    private StatementNode parseArithmeticStatement() {
//        // Ensure that there are enough tokens to represent an assignment statement
//        if (currentTokenIndex + 4 >= tokens.size()) {
//            error("Invalid arithmetic statement at " + tokens.get(currentTokenIndex).getPosition());
//            return null; // Or handle the error appropriately
//        }
//
//        // Check token sequence for arithmetic statement
//        String variableName = tokens.get(currentTokenIndex).getValue();
//        int startIndex = currentTokenIndex;
//
//        if (tokens.get(currentTokenIndex + 1).getType() == Token.Type.ASSIGNMENT) {
//            // Move to the next token after the assignment operator
//            currentTokenIndex += 2;
//        } else {
//            error("Invalid arithmetic statement at " + tokens.get(currentTokenIndex).getPosition());
//            return null;
//        }
//
//        System.out.println("Current Token: "  + tokens.get(currentTokenIndex));
//
//        int expressionStartIndex = currentTokenIndex;
//
//        // Parse the expression after the assignment operator
//        ExpressionNode expression = parseExpression();
//        currentTokenIndex = expressionStartIndex + expression.countTokens() - 1;
//
//        // Output or process the parsed arithmetic statement as needed
//        System.out.println("Arithmetic Statement: " + variableName + " = " + expression);
//        System.out.println("Current Token: "  + tokens.get(currentTokenIndex));
//
//        return new StatementNode(tokens.get(startIndex), expression);
//    }
//
//    private ExpressionNode parseExpression() {
//        ExpressionNode left = parseAdditionSubtraction();
//
//        return left;
//    }
//
//    private ExpressionNode parseAdditionSubtraction() {
//        ExpressionNode left = parseMultiplicationDivision();
//        while (check(Token.Type.OPERATOR, "+") || check(Token.Type.OPERATOR, "-")) {
//            Token operatorToken = advance();
//            ExpressionNode right = parseMultiplicationDivision();
//            left = new ExpressionNode.Binary(operatorToken, left, right);
//        }
//        return left;
//    }
//
//    private ExpressionNode parseMultiplicationDivision() {
//        ExpressionNode left = parsePrimary();
//        while (check(Token.Type.OPERATOR, "*") || check(Token.Type.OPERATOR, "/")) {
//            Token operatorToken = advance();
//            ExpressionNode right = parsePrimary();
//            left = new ExpressionNode.Binary(operatorToken, left, right);
//        }
//        return left;
//    }
//
//    private ExpressionNode parsePrimary() {
//        if (match(Token.Type.VALUE)) {
//            return new ExpressionNode.Literal(previous());
//        } else if (match(Token.Type.VARIABLE)) {
//            return new ExpressionNode.Variable(previous());
//        } else if (match(Token.Type.PARENTHESES)) {
//            ExpressionNode expression = parseExpression();
//            consume(Token.Type.PARENTHESES, ")", "Expect ')' after expression.");
//            return expression;
//        } else {
//            throw error("Expect primary expression.", peek());
//        }
//    }
//
//    private FunctionNode parseFunctionCall() {
//        String functionName = tokens.get(currentTokenIndex).getValue();
//        List<ASTNode> arguments = new ArrayList<>();
//        Position currentFunctionPosition = tokens.get(currentTokenIndex).getPosition();
//        currentTokenIndex++;
//
//        if (tokens.get(currentTokenIndex).getType() != Token.Type.COLON) {
//            error("Missing colon (:) after DISPLAY call at line " + tokens.get(currentTokenIndex).getPosition().getLine() + " position " + tokens.get(currentTokenIndex).getPosition().getPosition());
//        }
//
//        if (functionName == "DISPLAY") {
//            boolean start = true;
//
//            while (currentTokenIndex < (tokens.size()) && (
//                    tokens.get(currentTokenIndex).getType() == Token.Type.COLON ||
//                    tokens.get(currentTokenIndex).getType() == Token.Type.DELIMITER ||
//                    tokens.get(currentTokenIndex).getType() == Token.Type.CONCATENATION ||
//                    tokens.get(currentTokenIndex).getType() == Token.Type.SPECIAL_CHARACTER ||
//                    tokens.get(currentTokenIndex).getType() == Token.Type.DISPLAY_VARIABLE ||
//                    tokens.get(currentTokenIndex).getType() == Token.Type.STRING_LITERAL ))
//            {
//
//                if (tokens.get(currentTokenIndex).getType() == Token.Type.COLON) {
//                    start = false;
//                    currentTokenIndex++;
//                    continue;
//                }
//
//                if (tokens.get(currentTokenIndex).getType() == Token.Type.CONCATENATION) {
//                    if (start) {
//                        error("Cannot concatenate without any prior string literals or variables");
//                    }
//
//                    if (tokens.get(currentTokenIndex + 1).getType() != Token.Type.STRING_LITERAL &&
//                        tokens.get(currentTokenIndex + 1).getType() != Token.Type.DISPLAY_VARIABLE &&
//                        tokens.get(currentTokenIndex + 1).getType() != Token.Type.SPECIAL_CHARACTER) {
//                            error("Missing string literal/variable/special character in display concatenation");
//                    }
//                    currentTokenIndex++;
//                }
//
//                if (tokens.get(currentTokenIndex).getType() == Token.Type.SPECIAL_CHARACTER) {
//                    Token token = tokens.get(currentTokenIndex);
//
//                    if (tokens.get(currentTokenIndex).getValue() == "$") {
//                        SpecialCharacterNode specialCharacter = new SpecialCharacterNode(token.getValue(), token.getPosition());
//                        arguments.add(specialCharacter);
//                        currentTokenIndex++;
//                        continue;
//                    }
//
//                    if (tokens.get(currentTokenIndex).getValue() == "[") {
//
//                        System.out.println(tokens.get(currentTokenIndex + 1).getValue() + " 2nd " + tokens.get(currentTokenIndex + 2).getValue()  + " 3rd");
//
//                        if(tokens.get(currentTokenIndex + 1).getType() == Token.Type.VALUE &&
//                        tokens.get(currentTokenIndex + 2).getType() == Token.Type.SPECIAL_CHARACTER) {
//
//                            SpecialCharacterNode specialCharacter = new SpecialCharacterNode(tokens.get(currentTokenIndex + 1).getValue(), tokens.get(currentTokenIndex + 1).getPosition());
//                            arguments.add(specialCharacter);
//                            currentTokenIndex++;
//                            continue;
//                        }
//                    }
//                }
//
//                if (tokens.get(currentTokenIndex).getType() == Token.Type.DELIMITER) {
//                    if (tokens.get(currentTokenIndex + 1).getType() == Token.Type.STRING_LITERAL &&
//                        tokens.get(currentTokenIndex + 2).getType() == Token.Type.DELIMITER) {
//                            String value = tokens.get(currentTokenIndex + 1).getValue();
//                            StringLiteralNode newNode = new StringLiteralNode(value, tokens.get(currentTokenIndex + 1).getPosition());
//                            System.out.print("String Literal: " + value);
//                            arguments.add(newNode);
//                            currentTokenIndex += 3;
//                    } else {
//                        error("Missing delimiter in string literal");
//                    }
//                    continue;
//                }
//
//                if (tokens.get(currentTokenIndex).getType() == Token.Type.DISPLAY_VARIABLE) {
//                    String variableName = tokens.get(currentTokenIndex).getValue();
//                    VariableNode newNode  = new VariableNode(variableName, tokens.get(currentTokenIndex).getPosition());
//                    arguments.add(newNode);
//                    currentTokenIndex++;
//                    continue;
//                }
//
//                System.out.println("Found arguments: " + arguments.size());
//            }
//        }
//
//        return new FunctionNode(functionName, arguments, currentFunctionPosition);
//    }
//
//    // Helper methods for token handling
//    private boolean match(Token... tokens) {
//        for (Token token : tokens) {
//            if (check(token.getType(), token.getValue())) {
//                advance();
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private Token advance() {
//        if (!isAtEnd()) currentTokenIndex++;
//        return previous();
//    }
//
//    private boolean check(Token.Type type, String value) {
//        if (isAtEnd()) return false;
//        Token token = peek();
//        return token.getType() == type && token.getValue().equals(value);
//    }
//
//    private Token peek() {
//        return tokens.get(currentTokenIndex);
//    }
//
//    private Token previous() {
//        return tokens.get(currentTokenIndex - 1);
//    }
//
//    private boolean isAtEnd() {
//        return currentTokenIndex >= tokens.size();
//    }
//
//    private void consume(Token.Type type, String value, String message) {
//        if (check(type, value)) {
//            advance();
//        } else {
//            throw error(message);
//        }
//    }
//}