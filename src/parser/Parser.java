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
    private List<StatementNode> programStatements = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }

    public ProgramNode parse() {
        return parseProgram();
    }

    private ProgramNode parseProgram() {
        if (!match(Type.BEGIN_CODE)) {
            error("Expected BEGIN CODE", peek());
        }

        if (!match(Type.NEWLINE)) {
            error("EWLINE AFTER BEGIN CODE", peek());
        }

        if (!match(Type.INDENT)) {
            error("Expected INDENTION AFTER BEGIN CODE", peek());
        }

        parseDeclaration();

        if (!match(Type.DEDENT)) {
            error("Expected DEDENTION IN END CODE", peek());
        }

        if (!match(Type.END_CODE)) {
            error("Expected END CODE", peek());
        }

        if (!match(Type.NEWLINE)) {
            error("Expected NEWLINE AFTER END CODE", peek());
        }

        while (!match(Type.EOF)) {
            if (!match(Type.NEWLINE)) {
                error("Code should be enclosed within 'BEGIN CODE' and 'END CODE' markers. Found code outside this range.",
                        peek());
            }
        }

        if (!isAtEnd()) {
            error("Unexpected token", peek());
        }

        return new ProgramNode(declarations, programStatements);
    }

    private void parseDeclaration() {

        System.out.println("Parsing Declarations");

        while (match(Type.INT) || match(Type.CHAR) || match(Type.FLOAT) || match(Type.BOOL)) {
            declarations.addAll(parseVariableDeclaration());
        }

        if (declarations.size() == 0) {
            error("Invalid Data Type Detected", peek());
        }

        System.out.println("Parsing Statements");
        programStatements.addAll(parseStatements(false));
    }

    private List<VariableDeclarationNode> parseVariableDeclaration() {
        Token dataType = previous();
        List<VariableDeclarationNode> variables = new ArrayList<>();

        do {
            Token identifier = consume(Type.IDENTIFIER, "Expected identifier");
            String variableName = identifier.getLexeme();

            if (declaredVariableNames.contains(variableName)) {
                error("Variable " + variableName + " is already declared", identifier);
            }

            declaredVariableNames.add(variableName);

            if (match(Type.ASSIGNMENT)) {
                Token literal = consume(Type.LITERAL, "Expected a Literal");
                variables.add(new VariableDeclarationNode(dataType, identifier, literal));
            } else {
                variables.add(new VariableDeclarationNode(dataType, identifier));
            }

        } while (match(Type.COMMA));

        consume(Type.NEWLINE, "Expected a newline after variable declaration");

        return variables;
    }

    private List<StatementNode> parseStatements(boolean isIfStatement) {

        List<StatementNode> statements = new ArrayList<>();

        while (!match(Type.EOF) && !(currentTokenIndex >= tokens.size())) {

            if (match(Type.INT) || match(Type.CHAR) || match(Type.FLOAT) || match(Type.BOOL)) {
                error("Found a variable declaration after the executable code", previous());
            }

            if (match(Type.IDENTIFIER)) {
                if (peek().getType() == Type.ASSIGNMENT &&
                        (peekNext(2).getType() == Type.ADD ||
                                peekNext(2).getType() == Type.SUBTRACT ||
                                peekNext(2).getType() == Type.MULTIPLY ||
                                peekNext(2).getType() == Type.DIVIDE ||
                                peekNext(2).getType() == Type.MODULO)) {

                    statements.add(parseArithmeticStatement());
                    checkForNewline();

                } else if (peek().getType() == Type.ASSIGNMENT &&
                        (peekNext(1).getType() == Type.LEFT_PARENTHESIS)) {

                    statements.add(parseArithmeticStatement());
                    checkForNewline();

                } else {

                    statements.addAll(parseAssignmentStatement());
                    checkForNewline();
                }

                continue;
            }

            if (match(Type.DISPLAY)) {
                statements.add(parseDisplayStatement());
                checkForNewline();
                continue;
            }

            if (match(Type.SCAN)) {
                statements.add(parseScanStatement());
                checkForNewline();
                continue;
            }

            if (match(Type.IF)) {
                statements.addAll(parseIfStatement());
                System.out.println(statements.getLast());
                continue;
            }

            if (match(Type.WHILE)) {
                statements.add(parseWhileStatement());
                checkForNewline();
                continue;
            }

            if (match(Type.ELSE_IF)) {
                error("Found an ELSE_IF block without an IF block", previous());
            }

            if (match(Type.ELSE)) {
                error("Found an ELSE_IF block without an IF block", previous());
            }

            if (peek().getType() == Type.DEDENT) {
                if (isIfStatement) {
                    if (peekNext(1).getType() == Type.END_IF) {
                        return statements;
                    } else {
                        error("Expected END IF after DEDENTION", peek());
                    }
                } else {
                    if (peekNext(1).getType() == Type.END_CODE) {
                        return statements;
                    } else {
                        error("Expected END CODE after DEDENTION", peek());
                    }
                }
            }

            if (match(Type.DEDENT)) {
                error("Unexpected DEDENTION", peek());
            }

            if (match(Type.END_CODE)) {
                error("Unexpected END CODE", peek());
            }
        }

        return statements;
    }

    private List<StatementNode> parseAssignmentStatement() {
        List<StatementNode> assignments = new ArrayList<>();

        Token identifierToken = previous();
        VariableNode identifier = new VariableNode(identifierToken);

        if (!match(Type.ASSIGNMENT)) {
            error("Expected an assignment token.", peek());
        }

        if (match(Type.IDENTIFIER) && peek().getType() != Type.ASSIGNMENT) {

            assignments.add(new AssignmentNode(identifier, new VariableNode(previous())));
            return assignments;

        } else if (match(Type.LITERAL) && peek().getType() != Type.ASSIGNMENT) {

            assignments.add(new AssignmentNode(identifier, new LiteralNode(previous())));
            return assignments;

        } else {
            error("Assignment Operation Error. Expected a LITERAL or an IDENTIFIER after an assigment token.",
                    identifierToken);
        }

        List<Token> variableTokens = new ArrayList<>();
        variableTokens.add(identifierToken);

        while (match(Type.ASSIGNMENT)) {

            if (peek().getType() == Type.NEWLINE) {
                error("Expected an identifier or literal after an assigment token.", identifierToken);
            }

            if (match(Type.IDENTIFIER)) {

                Token var = previous();

                if (match(Type.ASSIGNMENT)) {

                    variableTokens.add(var);

                } else {

                    for (Token token : variableTokens) {
                        System.out.println("Variable: " + token.getLexeme());

                        VariableNode left = new VariableNode(token);
                        VariableNode right = new VariableNode(var);

                        assignments.add(new AssignmentNode(left, right));
                    }
                }

            } else if (match(Type.LITERAL)) {

                Token var = previous();

                if (match(Type.ASSIGNMENT)) {
                    error("Can't assign value to a Literal" + var, var);
                }

                for (Token token : variableTokens) {
                    VariableNode left = new VariableNode(token);
                    LiteralNode right = new LiteralNode(var);

                    assignments.add(new AssignmentNode(left, right));
                }

            }
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
        while (match(Type.MULTIPLY) || match(Type.DIVIDE) || match(Type.MODULO)) {
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

            if (!match(Type.RIGHT_PARENTHESIS)) {
                error("Expected ')' after expression.", peek());
            }

            return expression;

        } else if (match(Type.POSITIVE) || match(Type.NEGATIVE)) {

            Token operatorToken = previous();
            ExpressionNode expression = null;

            if (match(Type.LITERAL)) {
                expression = new LiteralNode(previous());
            } else if (match(Type.IDENTIFIER)) {
                expression = new VariableNode(previous());
            }
            return new UnaryNode(operatorToken, expression);
        } else {
            error("Expect primary expression.", peek());
        }

        return null;
    }

    private ExpressionNode parseConditionalExpression() {
        ExpressionNode left = parseExpression();

        if (match(Type.EQUAL) || match(Type.NOT_EQUAL) || match(Type.GREATER) ||
                match(Type.GREATER_EQUAL) || match(Type.LESS) || match(Type.LESS_EQUAL)) {

            Token operatorToken = previous();
            ExpressionNode right = parseExpression();

            return new BinaryNode(left, operatorToken, right);
        }

        return left;
    }

    private StatementNode parseDisplayStatement() {

        consume(Type.COLON, "Expected colon after Display Call");
        List<Token> arguments = new ArrayList<>();

        while (peek().getType() != Type.NEWLINE) {

            System.out.println("Current Token: " + peek());

            if (match(Type.CONCATENATION)) {

                if (arguments.size() == 0) {
                    error("Missing argument before concatenation", previous());
                }

                if (arguments.getLast().getType() == Type.CONCATENATION) {
                    error("Can't add another concatenation after a concatenation", previous());
                }

                arguments.add(previous());
            }

            if (match(Type.IDENTIFIER) || match(Type.NEXT_LINE)) {

                if (arguments.size() != 0 && arguments.getLast().getType() != Type.CONCATENATION) {
                    error("Can't add another argument without concatention", previous());
                }

                arguments.add(previous());
            }

            if (match(Type.ESCAPE_CODE_OPEN)) {

                if (arguments.size() != 0 && arguments.getLast().getType() != Type.CONCATENATION) {
                    error("Can't add another argument without concatention", previous());
                }

                arguments.add(consume(Type.SPECIAL_CHARACTER, "Expected special character after escape code open"));
                consume(Type.ESCAPE_CODE_CLOSE, "Expected escape code close");
            }

            if (match(Type.DELIMITER)) {

                if (arguments.size() != 0 && arguments.getLast().getType() != Type.CONCATENATION) {
                    error("Can't add another argument without concatention", previous());
                }

                arguments.add(consume(Type.STRING_LITERAL, "Expected string literal after delimiter"));

                consume(Type.DELIMITER, "Expected closing delimiter after the string literal");
            }
        }

        if (previous().getType() == Type.CONCATENATION) {
            error("Missing argument after concatenation symbol", previous());
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

    private List<StatementNode> parseIfStatement() {

        List<StatementNode> ifStatements = new ArrayList<>();
        Token token = previous();
        System.out.println("Parsing If Statement");

        // Expect '('
        consume(Type.LEFT_PARENTHESIS, "Expected '(', after 'IF' keyword");
        // Parse the conditional expression
        ExpressionNode ifCondition = parseConditionalExpression();

        // Expect ')'
        consume(Type.RIGHT_PARENTHESIS, "Expected ')', after the conditional expression");

        consume(Type.NEWLINE, "Expected new line before the BEGIN IF statement");

        consume(Type.BEGIN_IF, "Expected 'BEGIN IF' after the conditional expression");

        consume(Type.NEWLINE, "Expected new line after the BEGIN IF statement");

        consume(Type.INDENT, "Expected INDENT after the BEGIN IF statement");

        // Parse the body of the if statement
        List<StatementNode> body = parseStatements(true);

        consume(Type.DEDENT, "Expected DEDENTION after the body of the if statement");

        consume(Type.END_IF, "Expected 'END IF' after the body of the if statement");

        consume(Type.NEWLINE, "Expected new line after the END IF statement");

        ifStatements.add(new IfNode(ifCondition, body, token.getPosition()));

        if (match(Type.ELSE_IF)) {

            Token elseIfToken = previous();

            consume(Type.LEFT_PARENTHESIS, "Expected '(', after 'ELSE IF' keyword");
            // Parse the conditional expression
            ExpressionNode elseIfCondition = parseConditionalExpression();

            // Expect ')'
            consume(Type.RIGHT_PARENTHESIS, "Expected ')', after the conditional expression");

            consume(Type.NEWLINE, "Expected new line after the ELSE IF statement");

            consume(Type.BEGIN_IF, "Expected 'BEGIN ELSE IF' after the conditional expression");

            consume(Type.NEWLINE, "Expected new line after the BEGIN ELSE IF statement");

            consume(Type.INDENT, "Expected INDENT after the BEGIN ELSE IF statement");

            // Parse the body of the if statement
            List<StatementNode> elseIfBody = parseStatements(true);

            consume(Type.DEDENT, "Expected DEDENTION after the body of the if statement");

            consume(Type.END_IF, "Expected 'END ELSE IF' after the body of the if statement");

            consume(Type.NEWLINE, "Expected new line after the END IF statement");

            ifStatements.add(new ElseIfNode(elseIfCondition, elseIfBody, elseIfToken.getPosition()));
        }

        if (match(Type.ELSE)) {

            Token elseToken = previous();

            consume(Type.NEWLINE, "Expected new line after the END IF statement");
            consume(Type.BEGIN_IF, "Expected 'BEGIN ELSE' after the conditional expression");

            consume(Type.NEWLINE, "Expected new line after the BEGIN ELSE statement");

            consume(Type.INDENT, "Expected INDENT after the BEGIN ELSE statement");

            // Parse the body of the if statement
            List<StatementNode> elseBody = parseStatements(true);

            consume(Type.DEDENT, "Expected DEDENTION after the body of the if statement");

            consume(Type.END_IF, "Expected 'END ELSE' after the body of the if statement");

            consume(Type.NEWLINE, "Expected new line after the END IF statement");

            ifStatements.add(new ElseNode(elseBody, elseToken.getPosition()));
        }

        // Create and return the IfStatementNode
        return ifStatements;
    }

    private StatementNode parseWhileStatement() {
        return null;
    }

    private void checkForNewline() {
        if (!match(Type.NEWLINE)) {
            error("Expected a newline character after the statement. Please ensure each statement is on its own line.",
                    peek());
        }
    }

    private void error(String message, Token token) {
        // System.err.println("Syntax error " + token + ": " + message);
        // System.exit(1);

        // for debugging purposes so we know where the error is
        // Remove when checking
        throw new RuntimeException("Syntax error " + token + ": " + message);
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

            System.out.println("Consumed a Token: " + previous());

            return token;
        } else {
            error(errorMessage, token);
        }

        return null;
    }
}