package src.parser;
import java.util.ArrayList;
import java.util.List;
import src.utils.Token;
import src.utils.Position;
import src.nodes.ASTNode;
import src.nodes.ProgramNode;
import src.nodes.SpecialCharacterNode;
import src.nodes.VariableDeclarationNode;
import src.nodes.StatementNode;
import src.nodes.AssignmentStatementNode;
import src.nodes.StringLiteralNode;
import src.nodes.VariableNode;
import src.nodes.ExpressionNode;
import src.nodes.ExpressionNode.Literal;
import src.nodes.FunctionNode;

public class Parser {
    private final List<Token> tokens;
    private int currentTokenIndex = 0;
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
            error("Expected NEWLINE AFTER BEGIN CODE", peek());
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
            }
        } while (match(Token.Type.COMMA));
        
        return variables;
    }

    private void parseStatements() {
        while (!match(Token.Type.EOF)) {
            if (match(Token.Type.IDENTIFIER)) {
                statements.add(parseAssignmentStatement());
                continue;
            } 
            
            if(match(Token.Type.DISPLAY)) {
                statements.add(parseDisplayStatement());
                continue;
            }
            
            if(match(Token.Type.SCAN)) {
                statements.add(parseScanStatement());
                continue;
            }
            
            if(match(Token.Type.IF)) {
                statements.add(parseIfStatement());
                continue;
            }
            
            if(match(Token.Type.WHILE)) {
                statements.add(parseWhileStatement());
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

    private StatementNode parseAssignmentStatement() {
        Token identifier = previous();
        if (match(Token.Type.ASSIGNMENT)) {
            VariableNode variable = new VariableNode(identifier.getValue(), identifier.getPosition());
            ExpressionNode expression = parseExpression();

            if(!match(Token.Type.NEWLINE)) {
                error("Expected NEWLINE", peek());
            }

            return new AssignmentStatementNode(variable, expression, identifier.getPosition());
        } else {
            error("Expected assignment", peek());
        }
        return null;
    }

    private ExpressionNode parseExpression() {
        if (match(Token.Type.LEFT_PARENTHESIS)) {
            return parseParentheses();
        }
    
        if (match(Token.Type.INT_LITERAL) || match(Token.Type.FLOAT_LITERAL)) {
            Token left = previous();
            // Handle literals
            return new ExpressionNode.Literal(left);
        }
    
        if (match(Token.Type.IDENTIFIER)) {
            Token left = previous();
            // Handle variables
            return new ExpressionNode.Variable(left);
        }
    
        // Handle unary operations like NEGATIVE
        if (match(Token.Type.POSITIVE) || match(Token.Type.NEGATIVE)) {
            Token operator = previous();
            ExpressionNode rightExpression = parseExpression();
            return new ExpressionNode.Unary(operator, rightExpression);
        }
    
        // Handle binary operations like ADD, SUBTRACT, MULTIPLY, DIVIDE
        if (match(Token.Type.ADD) || match(Token.Type.SUBTRACT) || match(Token.Type.MULTIPLY) || match(Token.Type.DIVIDE)) {
            Token operator = previous();
            ExpressionNode leftExpression = parseExpression();
            ExpressionNode rightExpression = parseExpression();
            return new ExpressionNode.Binary(leftExpression, operator, rightExpression);
        }
    
        // If none of the above conditions match, it's a syntax error
        error("Invalid expression", peek());
        return null;
    }    

    private ExpressionNode parseArithmeticExpression(Token left) {

        ExpressionNode leftExpression = null;
        Token operator = previous();
    
        if(left.getType() == Token.Type.INT_LITERAL || left.getType() == Token.Type.FLOAT_LITERAL) {
            leftExpression = new ExpressionNode.Literal(left);
        } else {
            leftExpression = new ExpressionNode.Variable(left);
        }
        
        ExpressionNode rightExpression = parseExpression();
        
        return new ExpressionNode.Binary(leftExpression, operator, rightExpression);
    }

    private ExpressionNode parseParentheses() {
        ExpressionNode innerExpression = parseExpression();
        if(!match(Token.Type.RIGHT_PARENTHESIS)) {
            error("Expected RIGHT PARENTHESIS", peek());
        }
        return innerExpression;
    }

    private StatementNode parseDisplayStatement() {

        List<ASTNode> arguments = new ArrayList<>();
        Position currentFunctionPosition = tokens.get(currentTokenIndex).getPosition();

        if(!match(Token.Type.COLON)) {
            error("COLON not found after DISPLAY", peek());
        }
       
        boolean start = true;

        while (currentTokenIndex < (tokens.size()) && (peek().getType() != Token.Type.NEWLINE)) {

            if (match(Token.Type.CONCATENATION)) {
                if (start) {
                    error("Cannot concatenate without any prior string literals or variables", peek());
                }

                if (peek().getType() != Token.Type.STRING_LITERAL &&
                    peek().getType() != Token.Type.IDENTIFIER &&
                    peek().getType() != Token.Type.SPECIAL_CHARACTER){
                        error("Missing string literal/variable/special character in display concatenation", peek());
                }
            }

            if (match(Token.Type.SPECIAL_CHARACTER)) {
                Token token = previous();

                if (token.getValue() == "$") {
                    SpecialCharacterNode specialCharacter = new SpecialCharacterNode(token.getValue(), token.getPosition());
                    arguments.add(specialCharacter);
                    currentTokenIndex++;
                    continue;
                }

                if (token.getValue() == "[") {

                    if(peek().getType() == Token.Type.VALUE && peekNext(1).getType() == Token.Type.SPECIAL_CHARACTER) {
                        Token valueToken = consume(Token.Type.VALUE, "Expected value");
                        SpecialCharacterNode specialCharacter = new SpecialCharacterNode(valueToken.getValue(), valueToken.getPosition());
                        arguments.add(specialCharacter);
                        start = false;
                        continue;
                    }
                }
            }

            if (match(Token.Type.DELIMITER)) {
                if (peekNext(1).getType() == Token.Type.STRING_LITERAL &&
                    peekNext(2).getType() == Token.Type.DELIMITER) {
                        String value = tokens.get(currentTokenIndex + 1).getValue();
                        StringLiteralNode newNode = new StringLiteralNode(value, tokens.get(currentTokenIndex + 1).getPosition());
                        System.out.print("String Literal: " + value);
                        arguments.add(newNode);
                        currentTokenIndex += 2;
                } else {
                    error("Missing delimiter in string literal", peek());
                }
                continue;
            }

            if (match(Token.Type.IDENTIFIER)) {
                String variableName = previous().getValue();
                VariableNode newNode  = new VariableNode(variableName, tokens.get(currentTokenIndex).getPosition());
                arguments.add(newNode);
                continue;
            }
        }
        
        if(!match(Token.Type.NEWLINE)) {
            error("Expected One Statement per Line", peek());
        } else {
            System.out.println("Found a New Line after Display Statement");
        }

        System.out.println("Creating Display Node");
        return new FunctionNode("DISPLAY", arguments, currentFunctionPosition);
    }

    private StatementNode parseScanStatement() {
        return null;
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
