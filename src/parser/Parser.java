package src.parser;
import java.util.*;
import src.utils.Token;
import src.nodes.ProgramNode;
import src.nodes.VariableDeclarationNode;
import src.nodes.StatementNode;
import src.nodes.AssignmentStatementNode;
import src.nodes.VariableNode;
import src.nodes.ExpressionNode;
import src.nodes.FunctionNode;
import src.nodes.ExpressionNode;

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
        List<VariableDeclarationNode> declarations = new ArrayList<>();
        List<StatementNode> statements = new ArrayList<>();

        if(!match(Token.Type.BEGIN_CODE)) {
            error("Expected BEGIN CODE", peek());
        }

        while(currentTokenIndex < tokens.length() && peek().getType() != Token.Type.END_CODE) {
            if (match(Token.Type.NEWLINE)) {
                continue;
            }

            if (peek().getType() == Token.Type.INT ||
                peek().getType() == Token.Type.FLOAT ||
                peek().getType() == Token.Type.CHAR ||
                peek().getType() == Token.Type.BOOL) {
                    declarations.addAll(parseDeclaration());
                    continue;
            }

            if (peek().getType() == Token.Type.IDENTIFIER) {
                statements.add(parseStatement());
                continue;
            }

            if (peek().getType() == Token.Type.DISPLAY || 
                peek().getType() == Token.Type.SCAN) {
                    functionCalls.add(parseFunctionCall());
                    continue
            }
        }

        return new ProgramNode(declarations, statements, functionCalls);
    }

    private List<DeclarationNode> parseDeclaration() {
        List<DeclarationNode> variables = new ArrayList<>();
        String type = null;
        if (match(Token.Type.INT)) {
            type = "INT";
        } else if (match(Token.Type.CHAR)) {
            type = "CHAR";
        } else if (match(Token.Type.FLOAT)) {
            type = "FLOAT";
        } else if (match(Token.Type.BOOL)) {
            type = "BOOL";
        }
        // else {
        //     error("Expected type declaration (INT, CHAR)", peek());
        // }
        
        boolean expectIdentifier = true;

        while (!match(Token.Type.NEWLINE) && !isAtEnd()) {

            if (match(Token.Type.COMMA) && !expectIdentifier) {
                if (peek() == Token.Type.COMMA) {
                    error("Unexpected comma", peek());
                }
                expectIdentifier = true;
                continue;
            }

            if (peek().getType() == Token.Type.IDENTIFIER && !expectIdentifier) {
                error("Expected comma or newline", peek());
            }

            Token variable = consume(Token.Type.IDENTIFIER);
            Token value = null;
            expectIdentifier = false;

            if (match(Token.Type.EQUAL)) {
                if (type.equals("INT")) {
                    value = consume(Token.Type.INT_LITERAL);
                }

                else if (type.equals("FLOAT")) {
                    value = consume(Token.Type.FLOAT_LITERAL);
                }

                else if (type.equals("CHAR")) {
                    value = consume(Token.Type.CHAR_LITERAL);
                    String valStr = value.getValue();
                    if (valStr.length() > 3 || !valStr.startsWith("'") || !valStr.endsWith("'")) {
                        error("Invalid CHAR value", value);
                    }
                }

                else if (type.equals("BOOL")) {
                    value = consume(Token.Type.BOOL_LITERAL);
                    String valStr = value.getValue();
                    if (!valStr.equals("\"TRUE\"") || !valStr.equals("\"FALSE\"")) {
                        error("Invalid BOOL value", value);
                    }
                }
            }

            if (value == null) {
                variables.add(new DeclarationNode(type, variable.getValue(), null, variable.getPosition()));
            } else {
                variables.add(new DeclarationNode(type, variable.getValue(), value.getValue(), variable.getPosition()));
            }
        }
        return variables;
    }

    private StatementNode parseStatement() {
        Token variable = consume(Token.Type.IDENTIFIER);
        consume(Token.Type.ASSIGNMENT);
        
        // Start parsing an expression
        ExpressionNode expression = parseExpression();

        return new StatementNode(variable, expression);
    }
    
    private FunctionNode parseFunctionCall() {
        if (match(Token.Type.DISPLAY)) {
            Token function = previous();
            consume(Token.Type.COLON);
            List<Token> arguments = new ArrayList<>();

            while (peek().getType() != Token.Type.NEWLINE && !isAtEnd()) {
                Token current = peek();
                
                if (current.getType() == Token.Type.IDENTIFIER ||
                    current.getType() == Token.Type.INT_LITERAL ||
                    current.getType() == Token.Type.FLOAT_LITERAL ||
                    current.getType() == Token.Type.CHAR_LITERAL ||
                    current.getType() == Token.Type.BOOL_LITERAL) {
                        arguments.add(consume(current.getType()));

                        if (peek().getType() == Token.Type.CONCATENATION ||
                            peek().getType() == Token.Type.NEXT_LINE) {
                                arguments.add(consume(peek().getType()));

                        } else if (peek().getType() == Token.Type.ESCAPE_CODE_OPEN) {
                            consume(peek().getType());
                            arguments.add(consume(Token.Type.SPECIAL_CHARACTER));
                            consume(Token.Type.ESCAPE_CODE_CLOSE);

                        } else if (peek().getType() != Token.Type.NEWLINE) {
                            error("Expected concatenation symbol (&)", peek());
                        }

                    } else {
                        error("Expected an identifier or literal after concatenation symbol", peek());
                    }
            }

            if (previous.getType() == Token.Type.CONCATENATION) {
                error("Expected identifier or literal", previous());
            }

            return new FunctionCallNode(function.getValue(), arguments, function.getPosition());
        }
    }

    private ExpressionNode parseExpression() throws ParsingException {
        ExpressionNode result = null;
        Stack<ExpressionNode> stack = new Stack<>();
        
        while (peek().getType() != Token.Type.NEWLINE) {
            Token current = peek();
            switch (current.getType()) {
                case INT_LITERAL:
                case FLOAT_LITERAL:
                case BOOL_LITERAL:
                case CHAR_LITERAL:
                    stack.push(new ExpressionNode.Literal(consume(current.getType())));
                    break;
                case IDENTIFIER:
                    stack.push(new ExpressionNode.Variable(consume(Token.Type.IDENTIFIER)));
                    break;
                case LEFT_PARENTHESIS:
                    consume(Token.Type.LEFT_PARENTHESIS);
                    stack.push(parseExpression()); // Recursively parse expressions inside parentheses
                    consume(Token.Type.RIGHT_PARENTHESIS);
                    break;
                case ADD:
                case SUBTRACT:
                case MULTIPLY:
                case DIVIDE:
                    if (stack.size() < 1) throw new ParsingException("Invalid expression syntax");
                    consume(current.getType()); // consume the operator
                    ExpressionNode right = parseExpression(); // parse the expression after the operator
                    ExpressionNode left = stack.pop();
                    stack.push(new ExpressionNode.Binary(current, left, right));
                    break;
            }
        }

        if (stack.size() != 1) {
            error("Invalid expression formation", peak());
        }
        return stack.pop();
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

    private Token next() {
        return tokens.get(currentTokenIndex + 1);
    }
    
    private boolean isAtEnd() {
        return currentTokenIndex >= tokens.size();
    }

    private boolean match(Token.Type type) {
        // System.out.println("Current Token: " + tokens.get(currentTokenIndex));
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() == type) {
            return true;
        }
        return false;
    }

<<<<<<< HEAD
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
=======
    private Token consume(Token.Type type) {
        Token current = peek();
        if (match(type)) {
            return current;
        }
        throw new RuntimeException("Unexpected token: " + current);
>>>>>>> 550d1d1f3e2e86569a4946000de3f518917dff6d
    }
}
