package src.parser;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import src.utils.Token;
import src.nodes.ProgramNode;
import src.nodes.VariableDeclarationNode;
import src.nodes.StatementNode;
import src.nodes.AssignmentStatementNode;
import src.nodes.VariableNode;
import src.nodes.ExpressionNode;
import src.nodes.FunctionCallNode;
import src.nodes.ScanStatementNode;

public class Parser {
    private final List<Token> tokens;
    private int currentTokenIndex = 0;
    private Set<String> declaredVariableNames = new HashSet<>();
    private List<VariableDeclarationNode> declarations = new ArrayList<>();
    private List<StatementNode> statements = new ArrayList<>();

    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }

    public ProgramNode parse() {
        return parseProgram();
    }

    private ProgramNode parseProgram() {
        if(!match(Token.Type.BEGIN_CODE)) {
            error("Expected BEGIN CODE", peek());
        }

        if(!match(Token.Type.NEWLINE)) {
            error("EWLINE AFTER BEGIN CODE", peek());
        }

        if(!match(Token.Type.INDENT)) {
            error("Expected INDENTION AFTER BEGIN CODE", peek());
        }

        parseDeclaration();

        if(!match(Token.Type.NEWLINE)) {
            error("Expected NEWLINE AFTER END CODE", peek());
        }

        while(!match(Token.Type.EOF)) {
            if(!match(Token.Type.NEWLINE)) {
                error("Code should be enclosed within 'BEGIN CODE' and 'END CODE' markers. Found code outside this range.", peek());
            }
        }

        if (!isAtEnd()) {
            error("Unexpected token", peek());
        }

        return new ProgramNode(declarations, statements);
    }


    private void parseDeclaration() {
        while (!match(Token.Type.EOF)) {
            if (match(Token.Type.INT) || match(Token.Type.CHAR) || match(Token.Type.FLOAT) || match(Token.Type.BOOL)) {
                declarations.addAll(parseVariableDeclaration());

                if (!match(Token.Type.NEWLINE)) {
                    error("Expected NEWLINE", peek());
                }

            } else {
                if(declarations.size() == 0) {
                    error("Invalid Data Type Detected", peek());
                } else {
                    parseStatements();
                    break;
                }
            }
        }
    }

    private List<VariableDeclarationNode> parseVariableDeclaration() {
        Token.Type type = previous().getType();
        List<VariableDeclarationNode> variables = new ArrayList<>();

        do {
            Token identifier = consume(Token.Type.IDENTIFIER, "Expected identifier");
            String variableName = identifier.getValue();

            if (declaredVariableNames.contains(variableName)) {
                error("Variable " + variableName + " is already declared" , identifier);
            }

            declaredVariableNames.add(variableName);
            
            switch(type) {
                case Token.Type.INT:
                    if (match(Token.Type.ASSIGNMENT)) {
                        Token value = consume(Token.Type.INT_LITERAL, "Expected INT value");
                        variables.add(new VariableDeclarationNode("INT", identifier.getValue(), value.getValue(), identifier.getPosition()));
                    } else {
                        variables.add(new VariableDeclarationNode("INT", identifier.getValue(), identifier.getPosition()));
                    }
                    break;
                case Token.Type.CHAR:
                    if (match(Token.Type.ASSIGNMENT)) {
                        Token value = consume(Token.Type.CHAR_LITERAL, "Expected CHAR value");
                        variables.add(new VariableDeclarationNode("CHAR", identifier.getValue(), value.getValue(), identifier.getPosition()));
                    } else {
                        variables.add(new VariableDeclarationNode("CHAR", identifier.getValue(), identifier.getPosition()));
                    }
                    break;
                case Token.Type.FLOAT:
                    if(match(Token.Type.ASSIGNMENT)) {
                        Token value = consume(Token.Type.FLOAT_LITERAL, "Expected FLOAT value");
                        variables.add(new VariableDeclarationNode("FLOAT", identifier.getValue(), value.getValue(), identifier.getPosition()));
                    } else {
                        variables.add(new VariableDeclarationNode("FLOAT", identifier.getValue(), identifier.getPosition()));
                    } 
                    break;
                case Token.Type.BOOL:
                    if(match(Token.Type.ASSIGNMENT)) {
                        Token value = consume(Token.Type.BOOL_LITERAL, "Expected BOOL value");
                        variables.add(new VariableDeclarationNode("BOOL", identifier.getValue(), value.getValue(), identifier.getPosition()));
                    } else {
                        variables.add(new VariableDeclarationNode("BOOL", identifier.getValue(), identifier.getPosition()));
                    }
                    break;
                default:
                    error("Invalid Data Type", peek());
            }
        } while (match(Token.Type.COMMA));
        
        return variables;
    }

    private void parseStatements() {
        while (!match(Token.Type.EOF) && !(currentTokenIndex >= tokens.size())) {

            if (match(Token.Type.IDENTIFIER)) {
                if( 
                    peek().getType() == Token.Type.ASSIGNMENT && 
                    (peekNext(2).getType() == Token.Type.ADD || 
                    peekNext(2).getType() == Token.Type.SUBTRACT || 
                    peekNext(2).getType() == Token.Type.MULTIPLY || 
                    peekNext(2).getType() == Token.Type.DIVIDE)){

                    StatementNode statement = parseArithmeticStatement();
                    statements.add(statement);
                    
                    if (!match(Token.Type.NEWLINE)) {
                        error("Expected a newline character after the statement. Please ensure each statement is on its own line.", peek());
                    }

                }
                else if(peek().getType() == Token.Type.ASSIGNMENT && 
                        (peekNext(1).getType() == Token.Type.LEFT_PARENTHESIS)){
                    
                    StatementNode statement = parseArithmeticStatement();
                    statements.add(statement);

                    if (!match(Token.Type.NEWLINE)) {
                        error("Expected a newline character after the statement. Please ensure each statement is on its own line.", peek());
                    }

                }
                else {
                    List<StatementNode> statement = parseAssignmentStatement();
                    statements.addAll(statement);
            
                    if (!match(Token.Type.NEWLINE)) {
                        error("Expected a newline character after the statement. Please ensure each statement is on its own line.", peek());
                    }
                }

                continue;
            } 
            
            if(match(Token.Type.DISPLAY)) {
                statements.add(parseDisplayStatement());

                continue;
            }
            
            if(match(Token.Type.SCAN)) {
                statements.add(parseScanStatement());

                if (!match(Token.Type.NEWLINE)) {
                    error("Expected a newline character after the statement. Please ensure each statement is on its own line.", peek());
                }

                continue;
            }
            
            if(match(Token.Type.IF)) {
                statements.add(parseIfStatement());

                if (!match(Token.Type.NEWLINE)) {
                    error("Expected a newline character after the statement. Please ensure each statement is on its own line.", peek());
                }

                continue;
            }
            
            if(match(Token.Type.WHILE)) {
                statements.add(parseWhileStatement());

                if (!match(Token.Type.NEWLINE)) {
                    error("Expected a newline character after the statement. Please ensure each statement is on its own line.", peek());
                }

                continue;
            }

            if(match(Token.Type.DEDENT)){
                if(match(Token.Type.END_CODE)) {
                    break;  
                } else {
                    error("Expected END CODE after DEDENTION", peek());
                }
            }

            if(match(Token.Type.INT) || match(Token.Type.CHAR) || match(Token.Type.FLOAT) || match(Token.Type.BOOL)) {
                error("Found a variable declaration after the executable code", previous());
            }
            
            if(match(Token.Type.ELSE_IF)){
                error("Found an ELSE_IF block without an IF block", previous());
            }
        
            if(match(Token.Type.ELSE)) {
                error("Found an ELSE_IF block without an IF block", previous());
            }

            if(match(Token.Type.END_CODE)) {
                error("Unexpected END CODE without DEDENTION", peek());
            }
        }
    }

    private List<StatementNode> parseAssignmentStatement() {
        List<StatementNode> assignments = new ArrayList<>();
            
        // Parse assignment statement
        Token variableToken = previous();
        Token valueToken = tokens.get(currentTokenIndex + 1);

        VariableNode variable = new VariableNode(variableToken, valueToken.getPosition());
        ExpressionNode rightExpression = null;
;

        if(peek().getType() != Token.Type.ASSIGNMENT && tokens.get(currentTokenIndex + 1).getType() == Token.Type.IDENTIFIER) {
            rightExpression = new ExpressionNode.Variable(valueToken);

            boolean isDeclared = false;
            
            for (VariableDeclarationNode declaration: declarations) {
                if(declaration.getVariableName().equals(valueToken.getValue())) {
                    isDeclared = true;
                } 
            }

            if(variableToken != valueToken) {
                error("Type Mismatch", valueToken);
            }

            // Variable does not exist
            if(!isDeclared) {
                error("Variable not declared", valueToken);
            }
            
            assignments.add(new AssignmentStatementNode(variable, rightExpression, variableToken.getPosition()));
            currentTokenIndex += 2; // Skip over the assignment and the right side
            return assignments;

        } else if(peek().getType() != Token.Type.ASSIGNMENT && tokens.get(currentTokenIndex + 1).getType() == Token.Type.INT_LITERAL || tokens.get(currentTokenIndex + 1).getType() == Token.Type.FLOAT_LITERAL || tokens.get(currentTokenIndex + 1).getType() == Token.Type.BOOL_LITERAL || tokens.get(currentTokenIndex + 1).getType() == Token.Type.CHAR_LITERAL) {

            rightExpression = new ExpressionNode.Literal(valueToken);
            
            String variableDataType = null;
            for (VariableDeclarationNode declaration: declarations) {
                if(declaration.getVariableName().equals(variableToken.getValue())) {
                    variableDataType = declaration.getDataType();
                } 
            }

            switch (variableDataType) {
                case "INT":
                case "FLOAT":
                    if(!(valueToken.getType() == Token.Type.INT_LITERAL || valueToken.getType() == Token.Type.FLOAT_LITERAL)) {
                        error("Type Mismatch. Assigning " + valueToken.getType() + " to a " + variableDataType, valueToken);
                    }
                    break;
                case "BOOL":
                    if(!(valueToken.getType() == Token.Type.BOOL_LITERAL)) {
                        error("Type Mismatch. Assigning " + valueToken.getType() + " to a " + variableDataType, valueToken);
                    }
                    break;
                case "CHAR":
                    if(!(valueToken.getType() == Token.Type.CHAR_LITERAL)) {
                        error("Type Mismatch. Assigning " + valueToken.getType() + " to a " + variableDataType, valueToken);
                    }
                    break;
                default:
                    error("Data Type Invalid", valueToken);
            }

            
            assignments.add(new AssignmentStatementNode(variable, rightExpression, variableToken.getPosition()));
            currentTokenIndex += 2; // Skip over the assignment and the right side
            return assignments;
        }

        List<Token> variableTokens = new ArrayList<>();
        variableTokens.add(variableToken);

        while(match(Token.Type.ASSIGNMENT)) {

            if(peek().getType() == Token.Type.IDENTIFIER) {

                if(peekNext(1).getType() == Token.Type.ASSIGNMENT) {
                    
                    Token var = peek();

                    boolean isDeclared = false;

                    for (VariableDeclarationNode declaration: declarations) {
                        if(declaration.getVariableName().equals(var.getValue())) {
                            isDeclared = true;
                        }   
                    }

                    // Variable does not exist
                    if(!isDeclared) {
                        error("Variable not declared", var);
                    }

                    variableTokens.add(peek());
                
                    currentTokenIndex++;

                } else {


                    // Here the variable should have an initialization since it is the end
                    Token var = peek();
                    boolean isDeclared = false;


                    for (VariableDeclarationNode declaration: declarations) {
                        if(declaration.getVariableName().equals(var.getValue())) {
                            isDeclared = true;
                            if(declaration.getValue() == null) {
                                error("Variable not initialized", var);
                            }
                        }   
                    }

                    // Variable does not exist
                    if(!isDeclared) {
                        error("Variable not declared", peek());
                    }

                    valueToken = consume(Token.Type.IDENTIFIER, "Expected identifier");

                    for (Token token: variableTokens) {
                        System.out.println("Variable: " + token.getValue());

                        VariableNode v = new VariableNode(token, var.getPosition());
                        ExpressionNode e = new ExpressionNode.Variable(valueToken);

                        // Before Creating the AssignmentStatementNode make sure that they are the same data type
                        String variableDataType = null;
                        String expressionDataType = null;
                        for(VariableDeclarationNode declaration : declarations) {
                            if (declaration.getVariableName().equals(token.getValue())) {
                                variableDataType = declaration.getDataType();
                            }

                            if(declaration.getVariableName().equals(valueToken.getValue())) {
                                expressionDataType = declaration.getDataType();
                            }
                        }
                        
                        if(variableDataType.equals(expressionDataType)) {

                        } else{
                            error("Type Mismatch. Assigning " + expressionDataType + " to a " + variableDataType, valueToken);
                        }

                        assignments.add(new AssignmentStatementNode(v, e, variableToken.getPosition()));                               
                    }
                }

            } else if (peek().getType() == Token.Type.INT_LITERAL || peek().getType() == Token.Type.FLOAT_LITERAL || peek().getType() == Token.Type.BOOL_LITERAL || peek().getType() == Token.Type.CHAR_LITERAL) {
                rightExpression = new ExpressionNode.Literal(valueToken);

                if(peekNext(1).getType() == Token.Type.ASSIGNMENT) {
                    error("Can't assign value to a Literal", valueToken);
                } else {
                    for (Token token: variableTokens) {
                        VariableNode v = new VariableNode(token, token.getPosition());
                        ExpressionNode e = new ExpressionNode.Literal(peek());

                        // Before Creating the AssignmentStatementNode make sure that they are the same data type
                        String variableDataType = null;
                        for(VariableDeclarationNode declaration : declarations) {
                            if (declaration.getVariableName().equals(token.getValue())) {
                                variableDataType = declaration.getDataType();
                            }
                        }
                        
                        switch (peek().getType()) {
                            case INT_LITERAL:
                            case FLOAT_LITERAL:
                                if(!(variableDataType.equals("INT") || variableDataType.equals("FLOAT"))) {
                                    error("Type Mismatch. Assigning " + peek().getType() + " to a " + variableDataType, valueToken);
                                }
                                break;
                            case BOOL_LITERAL:
                                if(!(variableDataType.equals("BOOL"))) {
                                    error("Type Mismatch. Assigning " + peek().getType() + " to a " + variableDataType, valueToken);
                                }
                                break;
                            case CHAR_LITERAL:
                                if(!(variableDataType.equals("BOOL"))) {
                                    error("Type Mismatch. Assigning " + peek().getType() + " to a " + variableDataType, valueToken);
                                }
                                break;
                            default:
                                error("Data Type Invalid", valueToken);
                        }

                        assignments.add(new AssignmentStatementNode(v, e, variableToken.getPosition()));
                    }
    
                }
            
                currentTokenIndex++;

            } else {
                error("Assignment Operation Error", valueToken);
            }
        }

        return assignments;
    }

    private StatementNode parseArithmeticStatement() {
        // Ensure that there are enough tokens to represent an assignment statement
        if (currentTokenIndex + 4 >= tokens.size()) {
            error("Invalid arithmetic statement", peek());
            return null; // Or handle the error appropriately
        }
    
        // Check token sequence for arithmetic statement
        Token variableName = previous();
        int startIndex = currentTokenIndex;
    
        if (peek().getType() == Token.Type.ASSIGNMENT) {
            // Move to the next token after the assignment operator
            currentTokenIndex += 1;
        } else {
            error("Invalid arithmetic statement", peek());
            return null;
        }
    
        // Parse the expression after the assignment operator
        ExpressionNode expression = parseExpression();

        // Output or process the parsed arithmetic statement as needed
        System.out.println("Arithmetic Statement: " + variableName + " = " + expression);
        System.out.println("Current Token: "  + peek());
        
        VariableNode variable = new VariableNode(variableName, tokens.get(startIndex).getPosition());

        return new AssignmentStatementNode(variable, expression, tokens.get(startIndex).getPosition());
    }

    private ExpressionNode parseExpression() {
        ExpressionNode left = parseAdditionSubtraction();

        return left;
    }
    
    private ExpressionNode parseAdditionSubtraction() {
        ExpressionNode left = parseMultiplicationDivision();
        while (match(Token.Type.ADD) || match(Token.Type.SUBTRACT)) {
            Token operatorToken = previous();
            ExpressionNode right = parseMultiplicationDivision();
            left = new ExpressionNode.Binary(operatorToken, left, right);
        }
        return left;
    }
    
    private ExpressionNode parseMultiplicationDivision() {
        ExpressionNode left = parsePrimary();
        while (match(Token.Type.MULTIPLY) || match(Token.Type.DIVIDE)) {
            Token operatorToken = previous();
            ExpressionNode right = parsePrimary();
            left = new ExpressionNode.Binary(operatorToken, left, right);
        }
        return left;
    }
    
    private ExpressionNode parsePrimary() {
        if (match(Token.Type.INT_LITERAL) || match(Token.Type.FLOAT_LITERAL) || match(Token.Type.BOOL_LITERAL) || match(Token.Type.CHAR_LITERAL)){
            return new ExpressionNode.Literal(previous());
        } else if (match(Token.Type.IDENTIFIER)) {

            boolean isDeclared = false;

            for(VariableDeclarationNode var : declarations) {
                if(var.getVariableName().equals(previous().getValue())) {
                    isDeclared = true;
                    
                    if(var.getValue() == null) {
                        error("Variable not initialized", previous());
                    }

                    if(!(var.getDataType().equals("INT") || var.getDataType().equals("FLOAT"))) {
                        error("Arithmetic operation cannot be performed on "+ var.getDataType() + " data type", previous());
                    }
                    break;
                }
            }
            
            if (!isDeclared) {
                error("Variable not declared", previous());
            }

            return new ExpressionNode.Variable(previous());

        } else if (match(Token.Type.LEFT_PARENTHESIS)) {
            ExpressionNode expression = parseExpression();
            boolean found = false;

            while (match(Token.Type.RIGHT_PARENTHESIS)) {
                found = true;
            }
            if(!found) {
                error("Expected closing parenthesis", peek());
            }
            return expression;

        } else if (match(Token.Type.POSITIVE) || match(Token.Type.NEGATIVE)) {
            
            Token operatorToken = previous();
            ExpressionNode expression = null;

            if(match(Token.Type.INT_LITERAL) || match(Token.Type.FLOAT_LITERAL)) {
                expression = new ExpressionNode.Literal(previous());
            } else if(match(Token.Type.IDENTIFIER)) {
                expression = new ExpressionNode.Variable(previous());
            }
            return new ExpressionNode.Unary(operatorToken, expression);
        }
        else {
            error("Expect primary expression.", peek());
        }

        return null;
    }

    private StatementNode parseDisplayStatement() {
        
        Token function = previous();
        consume(Token.Type.COLON, "Expected colon after Display Call");
        List<Token> arguments = new ArrayList<>();

        while (peek().getType() != Token.Type.NEWLINE && !isAtEnd()) {
            
            System.out.println("Parsing Display Statement");

            Token current = peek();

            System.out.println("Peek: " + peek());

            if (current.getType() == Token.Type.IDENTIFIER ||
                current.getType() == Token.Type.INT_LITERAL ||
                current.getType() == Token.Type.FLOAT_LITERAL ||
                current.getType() == Token.Type.CHAR_LITERAL ||
                current.getType() == Token.Type.BOOL_LITERAL) {

                System.out.println("TEST");

                arguments.add(consume(current.getType(), "Expected identifier or literal"));

                if (peek().getType() == Token.Type.CONCATENATION ||
                    peek().getType() == Token.Type.NEXT_LINE) {
                        arguments.add(consume(peek().getType(), "Expected concatenation symbol or newline"));

                } else if (peek().getType() == Token.Type.ESCAPE_CODE_OPEN) {
                    arguments.add(peek());
                    currentTokenIndex++;
                    arguments.add(consume(Token.Type.SPECIAL_CHARACTER, "Expected special character after escape code open"));
                    consume(Token.Type.ESCAPE_CODE_CLOSE, "Expected escape code close");

                } else if (peek().getType() != Token.Type.NEWLINE) {
                    error("Expected concatenation symbol (&)", peek());
                }
            } else {
                error("Expected an identifier or literal after concatenation symbol", peek());
            }
        }

        if (previous().getType() == Token.Type.CONCATENATION) {
            error("Expected identifier or literal", previous());
        }

        if(!match(Token.Type.NEWLINE)) {
            error("One Statement per Line only", peek());
        }

        return new FunctionCallNode(function.getValue(), arguments, function.getPosition());
    }

    private StatementNode parseScanStatement() {
        consume(Token.Type.COLON, "Expected a COLON Token"); // Consume the colon ":" after SCAN

        List<String> identifiers = new ArrayList<>();

        // Parse the list of identifiers after the colon
        while (match(Token.Type.IDENTIFIER)) {
            identifiers.add(previous().getValue());
            // Check for comma to parse multiple identifiers
            if (!match(Token.Type.COMMA)) {
                break; // Exit loop if no comma found
            }
        }

        // Create a SCAN statement node with the list of identifiers
        return new ScanStatementNode(identifiers);
    }

    private StatementNode parseIfStatement() {
        return null;
    }

    private StatementNode parseWhileStatement() {
        return null;
    }

    private void error(String message, Token token) {
        System.err.println("Syntax error " + token + ": " + message);
        System.exit(1);
    }

    private Token peek() {
        return tokens.get(currentTokenIndex);
    }
    
    private Token previous() {
        return tokens.get(currentTokenIndex - 1);
    }
    
    private boolean isAtEnd() {
        return currentTokenIndex >= tokens.size();
    }

    private boolean match(Token.Type type) {
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() == type) {
            currentTokenIndex++;
            return true;
        }
        return false;
    }

    private Token peekNext(int index) {
        return tokens.get(currentTokenIndex + index);
    }

    private Token consume(Token.Type expectedType, String errorMessage) {
        Token token = peek();
        if (token.getType() == expectedType) {
            currentTokenIndex++;
            return token;
        } else {
            error(errorMessage, token);
        }

        return null;
    }
}