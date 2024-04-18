package src.parser;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import src.utils.Token;
import src.utils.Type;
import src.nodes.*;

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
        
        VariableNode identifier = new VariableNode(identifierToken);
        
        List<Token> variableTokens = new ArrayList<>();
        variableTokens.add(identifierToken);

        while(match(Type.ASSIGNMENT)) {

            if(match(Type.IDENTIFIER)) {

                Token var = previous();

                if(match(Type.ASSIGNMENT)) {
                    
                    variableTokens.add(var);
                
                } else {

                    for (Token token: variableTokens) {
                        System.out.println("Variable: " + token.getLexeme());

                        VariableNode left = new VariableNode(token);
                        VariableNode right = new VariableNode(var);

                        assignments.add(new AssignmentNode(left, right));                               
                    }
                }

            } else if (match(Type.LITERAL)) {
                
                Token var = previous();

                if(match(Type.ASSIGNMENT)) {
                    error("Can't assign value to a Literal" + var, var);
                } else {
                    for (Token token: variableTokens) {
                        VariableNode left = new VariableNode(token);
                        LiteralNode right = new LiteralNode(var);

                        assignments.add(new AssignmentNode(left, right));
                    }
    
                }
            
                currentTokenIndex++;

            } else {
                error("Assignment Operation Error. Expected a LITERAL or an IDENTIFIER", identifierToken);
            }
        }

        if(match(Type.IDENTIFIER)) {
    
            assignments.add(new AssignmentNode(identifier, new VariableNode(previous())));
            return assignments;

        } else if(match(Type.LITERAL)){

            assignments.add(new AssignmentNode(identifier, new LiteralNode(previous())));
            return assignments;
        }

        return assignments;
    }

    private StatementNode parseArithmeticStatement() {
        // Ensure that there are enough tokens to represent an assignment statement
        if (currentTokenIndex + 4 >= tokens.size()) {
            error("Invalid arithmetic statement", peek());
        }
    
        Token variableName = previous();
    
        if (!match(Type.ASSIGNMENT)) {
            error("Invalid arithmetic statement", peek());
            return null;
        }
    
        VariableNode variable = new VariableNode(variableName);
        ExpressionNode expression = parseExpression();

        return new AssignmentNode(variable, expression);
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
            left = new BinaryNode(left, operatorToken, right);
        }
        return left;
    }
    
    private ExpressionNode parseMultiplicationDivision() {
        ExpressionNode left = parsePrimary();
        while (match(Type.MULTIPLY) || match(Type.DIVIDE)) {
            Token operatorToken = previous();
            ExpressionNode right = parsePrimary();
            left = new BinaryNode(left, operatorToken, right);
        }
        return left;
    }
    
    private ExpressionNode parsePrimary() {
        if (match(Type.LITERAL)) {
            return new LiteralNode(previous());
        } else if (match(Type.IDENTIFIER)) {
            return new VariableNode(previous());
        } else if (match(Type.LEFT_PARENTHESIS)) {
            ExpressionNode expression = parseExpression();
            return expression;

        } else if (match(Type.POSITIVE) || match(Type.NEGATIVE)) {
            
            Token operatorToken = previous();
            ExpressionNode expression = null;

            if(match(Type.LITERAL)) {
                expression = new LiteralNode(previous());
            } else if(match(Type.IDENTIFIER)) {
                expression = new VariableNode(previous());
            }
            return new UnaryNode(operatorToken, expression);
        }
        else {
            error("Expect primary expression.", peek());
        }

        return null;
    }

    private StatementNode parseDisplayStatement() {
        
        consume(Type.COLON, "Expected colon after Display Call");
        List<Token> arguments = new ArrayList<>();

        while (!match(Type.NEWLINE)) {

            if (match(Type.IDENTIFIER)) {

                arguments.add(previous());

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

        return new DisplayNode(arguments);
    }

    private StatementNode parseScanStatement() {
        Token scanToken = previous();

        consume(Type.COLON, "Expected a COLON Token"); // Consume the colon ":" after SCAN

        List<Token> identifiers = new ArrayList<>();

        // Parse the list of identifiers after the colon
        while (match(Type.IDENTIFIER)) {
            identifiers.add(previous());
            // Check for comma to parse multiple identifiers
            if (!match(Type.COMMA)) {
                break; // Exit loop if no comma found
            }
        }

        // Create a SCAN statement node with the list of identifiers
        return new ScanNode(identifiers, scanToken.getPosition());
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