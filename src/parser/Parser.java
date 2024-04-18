package src.parser;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import src.utils.Token;
import src.utils.Type;
import src.nodes.ProgramNode;
import src.nodes.VariableDeclarationNode;
import src.nodes.StatementNode;
import src.nodes.AssignmentNode;
import src.nodes.ExpressionNode;
import src.nodes.DisplayNode;
import src.nodes.ScanNode;

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
        if(!match(Type.BEGIN_CODE)) {
            error("Expected BEGIN CODE", peek());
        }

        if(!match(Type.NEWLINE)) {
            error("EWLINE AFTER BEGIN CODE", peek());
        }

        if(!match(Type.INDENT)) {
            error("Expected INDENTION AFTER BEGIN CODE", peek());
        }

        parseDeclaration();

        if(!match(Type.NEWLINE)) {
            error("Expected NEWLINE AFTER END CODE", peek());
        }

        while(!match(Type.EOF)) {
            if(!match(Type.NEWLINE)) {
                error("Code should be enclosed within 'BEGIN CODE' and 'END CODE' markers. Found code outside this range.", peek());
            }
        }

        if (!isAtEnd()) {
            error("Unexpected token", peek());
        }

        return new ProgramNode(declarations, statements);
    }


    private void parseDeclaration() {
        while (!match(Type.EOF)) {
            if (match(Type.INT) || match(Type.CHAR) || match(Type.FLOAT) || match(Type.BOOL)) {
                declarations.addAll(parseVariableDeclaration());

                if (!match(Type.NEWLINE)) {
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
        Token dataType = previous();
        List<VariableDeclarationNode> variables = new ArrayList<>();

        do {
            Token identifier = consume(Type.IDENTIFIER, "Expected identifier");
            String variableName = identifier.getLexeme();

            if (declaredVariableNames.contains(variableName)) {
                error("Variable " + variableName + " is already declared" , identifier);
            }

            declaredVariableNames.add(variableName);
            
            
            if (match(Type.ASSIGNMENT)) {
                Token literal = consume(Type.LITERAL, "Expected a Literal");
                variables.add(new VariableDeclarationNode(dataType, identifier, literal));
            } else {
                variables.add(new VariableDeclarationNode(dataType, identifier));
            }

        } while (match(Type.COMMA));
        
        return variables;
    }

    private void parseStatements() {
        while (!match(Type.EOF) && !(currentTokenIndex >= tokens.size())) {

            if (match(Type.IDENTIFIER)) {
                if( 
                    peek().getType() == Type.ASSIGNMENT && 
                    (peekNext(2).getType() == Type.ADD || 
                    peekNext(2).getType() == Type.SUBTRACT || 
                    peekNext(2).getType() == Type.MULTIPLY || 
                    peekNext(2).getType() == Type.DIVIDE)){

                    StatementNode statement = parseArithmeticStatement();
                    statements.add(statement);
                    
                    if (!match(Type.NEWLINE)) {
                        error("Expected a newline character after the statement. Please ensure each statement is on its own line.", peek());
                    }

                }
                else if(peek().getType() == Type.ASSIGNMENT && 
                        (peekNext(1).getType() == Type.LEFT_PARENTHESIS)){
                    
                    StatementNode statement = parseArithmeticStatement();
                    statements.add(statement);

                    if (!match(Type.NEWLINE)) {
                        error("Expected a newline character after the statement. Please ensure each statement is on its own line.", peek());
                    }

                }
                else {
                    List<StatementNode> statement = parseAssignmentStatement();
                    statements.addAll(statement);
            
                    if (!match(Type.NEWLINE)) {
                        error("Expected a newline character after the statement. Please ensure each statement is on its own line.", peek());
                    }
                }

                continue;
            } 
            
            if(match(Type.DISPLAY)) {
                statements.add(parseDisplayStatement());

                continue;
            }
            
            if(match(Type.SCAN)) {
                statements.add(parseScanStatement());

                if (!match(Type.NEWLINE)) {
                    error("Expected a newline character after the statement. Please ensure each statement is on its own line.", peek());
                }

                continue;
            }
            
            if(match(Type.IF)) {
                statements.add(parseIfStatement());

                if (!match(Type.NEWLINE)) {
                    error("Expected a newline character after the statement. Please ensure each statement is on its own line.", peek());
                }

                continue;
            }
            
            if(match(Type.WHILE)) {
                statements.add(parseWhileStatement());

                if (!match(Type.NEWLINE)) {
                    error("Expected a newline character after the statement. Please ensure each statement is on its own line.", peek());
                }

                continue;
            }

            if(match(Type.DEDENT)){
                if(match(Type.END_CODE)) {
                    break;  
                } else {
                    error("Expected END CODE after DEDENTION", peek());
                }
            }

            if(match(Type.INT) || match(Type.CHAR) || match(Type.FLOAT) || match(Type.BOOL)) {
                error("Found a variable declaration after the executable code", previous());
            }
            
            if(match(Type.ELSE_IF)){
                error("Found an ELSE_IF block without an IF block", previous());
            }
        
            if(match(Type.ELSE)) {
                error("Found an ELSE_IF block without an IF block", previous());
            }

            if(match(Type.END_CODE)) {
                error("Unexpected END CODE without DEDENTION", peek());
            }
        }
    }

    private List<StatementNode> parseAssignmentStatement() {
        List<StatementNode> assignments = new ArrayList<>();
            
        // Parse assignment statement
        Token identifierToken = previous();

        if(!match(Type.ASSIGNMENT)) {
            error("Expected an assignment token. Found a ", peek());
        }

        Token literalToken = consume(Type.LITERAL, "Expected A Literal");
        
        ExpressionNode.Variable identifier = new ExpressionNode.Variable(identifierToken);
        ExpressionNode rightExpression = null;
        
        while(match(Type.ASSIGNMENT)) {

            if(peek().getType() == Type.IDENTIFIER) {

                if(peekNext(1).getType() == Type.ASSIGNMENT) {
                    
                    Token var = peek();

                    boolean isDeclared = false;

                    for (VariableDeclarationNode declaration: declarations) {
                        if(declaration.getVariableName().equals(var.getLexeme())) {
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
                        if(declaration.getVariableName().equals(var.getLexeme())) {
                            isDeclared = true;
                            if(declaration.getLexeme() == null) {
                                error("Variable not initialized", var);
                            }
                        }   
                    }

                    // Variable does not exist
                    if(!isDeclared) {
                        error("Variable not declared", peek());
                    }

                    valueToken = consume(Type.IDENTIFIER, "Expected identifier");

                    for (Token token: variableTokens) {
                        System.out.println("Variable: " + token.getLexeme());

                        VariableNode v = new VariableNode(token, var.getPosition());
                        ExpressionNode e = new ExpressionNode.Variable(valueToken);

                        // Before Creating the AssignmentNode make sure that they are the same data type
                        String variableDataType = null;
                        String expressionDataType = null;
                        for(VariableDeclarationNode declaration : declarations) {
                            if (declaration.getVariableName().equals(token.getLexeme())) {
                                variableDataType = declaration.getDataType();
                            }

                            if(declaration.getVariableName().equals(valueToken.getLexeme())) {
                                expressionDataType = declaration.getDataType();
                            }
                        }
                        
                        if(variableDataType.equals(expressionDataType)) {

                        } else{
                            error("Type Mismatch. Assigning " + expressionDataType + " to a " + variableDataType, valueToken);
                        }

                        assignments.add(new AssignmentNode(v, e, variableToken.getPosition()));                               
                    }
                }

            } else if (peek().getType() == Type.INT_LITERAL || peek().getType() == Type.FLOAT_LITERAL || peek().getType() == Type.BOOL_LITERAL || peek().getType() == Type.CHAR_LITERAL) {
                rightExpression = new ExpressionNode.Literal(valueToken);

                if(peekNext(1).getType() == Type.ASSIGNMENT) {
                    error("Can't assign value to a Literal", valueToken);
                } else {
                    for (Token token: variableTokens) {
                        VariableNode v = new VariableNode(token, token.getPosition());
                        ExpressionNode e = new ExpressionNode.Literal(peek());

                        // Before Creating the AssignmentNode make sure that they are the same data type
                        String variableDataType = null;
                        for(VariableDeclarationNode declaration : declarations) {
                            if (declaration.getVariableName().equals(token.getLexeme())) {
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

                        assignments.add(new AssignmentNode(v, e, variableToken.getPosition()));
                    }
    
                }
            
                currentTokenIndex++;

            } else {
                error("Assignment Operation Error", valueToken);
            }
        }

        if(match(Type.IDENTIFIER)) {
            rightExpression = new ExpressionNode.Variable(previous());

            
            assignments.add(new AssignmentNode(identifier, rightExpression));
            currentTokenIndex += 2;
            return assignments;

        } else if(peek().getType() != Type.ASSIGNMENT && tokens.get(currentTokenIndex + 1).getType() == Type.INT_LITERAL || tokens.get(currentTokenIndex + 1).getType() == Type.FLOAT_LITERAL || tokens.get(currentTokenIndex + 1).getType() == Type.BOOL_LITERAL || tokens.get(currentTokenIndex + 1).getType() == Type.CHAR_LITERAL) {

            rightExpression = new ExpressionNode.Literal(valueToken);
            
            String variableDataType = null;
            
            assignments.add(new AssignmentNode(variable, rightExpression, variableToken.getPosition()));
            currentTokenIndex += 2; // Skip over the assignment and the right side
            return assignments;
        }

        List<Token> variableTokens = new ArrayList<>();
        variableTokens.add(variableToken);

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
    
        if (peek().getType() == Type.ASSIGNMENT) {
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

        return new AssignmentNode(variable, expression, tokens.get(startIndex).getPosition());
    }

    private ExpressionNode parseExpression() {
        ExpressionNode left = parseAdditionSubtraction();

        return left;
    }
    
    private ExpressionNode parseAdditionSubtraction() {
        ExpressionNode left = parseMultiplicationDivision();
        while (match(Type.ADD) || match(Type.SUBTRACT)) {
            Token operatorToken = previous();
            ExpressionNode right = parseMultiplicationDivision();
            left = new ExpressionNode.BinaryNode(operatorToken, left, right);
        }
        return left;
    }
    
    private ExpressionNode parseMultiplicationDivision() {
        ExpressionNode left = parsePrimary();
        while (match(Type.MULTIPLY) || match(Type.DIVIDE)) {
            Token operatorToken = previous();
            ExpressionNode right = parsePrimary();
            left = new ExpressionNode.BinaryNode(operatorToken, left, right);
        }
        return left;
    }
    
    private ExpressionNode parsePrimary() {
        if (match(Type.INT_LITERAL) || match(Type.FLOAT_LITERAL) || match(Type.BOOL_LITERAL) || match(Type.CHAR_LITERAL)){
            return new ExpressionNode.Literal(previous());
        } else if (match(Type.IDENTIFIER)) {

            boolean isDeclared = false;

            for(VariableDeclarationNode var : declarations) {
                if(var.getVariableName().equals(previous().getLexeme())) {
                    isDeclared = true;
                    
                    if(var.getLexeme() == null) {
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

        } else if (match(Type.LEFT_PARENTHESIS)) {
            ExpressionNode expression = parseExpression();
            boolean found = false;

            while (match(Type.RIGHT_PARENTHESIS)) {
                found = true;
            }
            if(!found) {
                error("Expected closing parenthesis", peek());
            }
            return expression;

        } else if (match(Type.POSITIVE) || match(Type.NEGATIVE)) {
            
            Token operatorToken = previous();
            ExpressionNode expression = null;

            if(match(Type.INT_LITERAL) || match(Type.FLOAT_LITERAL)) {
                expression = new ExpressionNode.Literal(previous());
            } else if(match(Type.IDENTIFIER)) {
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
        consume(Type.COLON, "Expected colon after Display Call");
        List<Token> arguments = new ArrayList<>();

        while (peek().getType() != Type.NEWLINE && !isAtEnd()) {
            
            System.out.println("Parsing Display Statement");

            Token current = peek();

            System.out.println("Peek: " + peek());

            if (current.getType() == Type.IDENTIFIER ||
                current.getType() == Type.INT_LITERAL ||
                current.getType() == Type.FLOAT_LITERAL ||
                current.getType() == Type.CHAR_LITERAL ||
                current.getType() == Type.BOOL_LITERAL) {

                System.out.println("TEST");

                arguments.add(consume(current.getType(), "Expected identifier or literal"));

                if (peek().getType() == Type.CONCATENATION ||
                    peek().getType() == Type.NEXT_LINE) {
                        arguments.add(consume(peek().getType(), "Expected concatenation symbol or newline"));

                } else if (peek().getType() == Type.ESCAPE_CODE_OPEN) {
                    arguments.add(peek());
                    currentTokenIndex++;
                    arguments.add(consume(Type.SPECIAL_CHARACTER, "Expected special character after escape code open"));
                    consume(Type.ESCAPE_CODE_CLOSE, "Expected escape code close");

                } else if (peek().getType() != Type.NEWLINE) {
                    error("Expected concatenation symbol (&)", peek());
                }
            } else {
                error("Expected an identifier or literal after concatenation symbol", peek());
            }
        }

        if (previous().getType() == Type.CONCATENATION) {
            error("Expected identifier or literal", previous());
        }

        if(!match(Type.NEWLINE)) {
            error("One Statement per Line only", peek());
        }

        return new DisplayNode(function.getLexeme(), arguments, function.getPosition());
    }

    private StatementNode parseScanStatement() {
        consume(Type.COLON, "Expected a COLON Token"); // Consume the colon ":" after SCAN

        List<String> identifiers = new ArrayList<>();

        // Parse the list of identifiers after the colon
        while (match(Type.IDENTIFIER)) {
            identifiers.add(previous().getLexeme());
            // Check for comma to parse multiple identifiers
            if (!match(Type.COMMA)) {
                break; // Exit loop if no comma found
            }
        }

        // Create a SCAN statement node with the list of identifiers
        return new ScanNode(identifiers);
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

    private boolean match(Type type) {
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() == type) {
            currentTokenIndex++;
            return true;
        }
        return false;
    }

    private Token peekNext(int index) {
        return tokens.get(currentTokenIndex + index);
    }

    private Token consume(Type expectedType, String errorMessage) {
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