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
            error("Expected NEWLINE AFTER BEGIN CODE", peek());
        }

        if (!match(Type.INDENT)) {

            if (match(Type.END_CODE)) {

                System.out.println("Was here" + peek().getPosition());

                consume(Type.NEWLINE, "Expected NEWLINE AFTER END CODE");
                consume(Type.EOF, "Expected EOF after END CODE but found " + peek().getType());
                return new ProgramNode(declarations, programStatements);
            }

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
        programStatements.addAll(parseStatements(false, false));
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
                Token unary = null;
                if (match(Type.NEGATIVE) || match(Type.POSITIVE)) {
                    unary = previous();
                    Token literal = consume(Type.LITERAL, "Expected literal after assignment token");

                    variables.add(new VariableDeclarationNode(dataType, identifier,
                            new Token(Type.LITERAL, unary.getLexeme() + literal.getLexeme(), null)));
                } else {
                    Token literal = consume(Type.LITERAL, "Expected literal after assignment token");

                    variables.add(new VariableDeclarationNode(dataType, identifier,
                            new Token(Type.LITERAL, literal.getLexeme(), null)));
                }

            } else {
                variables.add(new VariableDeclarationNode(dataType, identifier));
            }

        } while (match(Type.COMMA));

        consume(Type.NEWLINE, "Expected a newline after variable declaration");

        return variables;
    }

    private List<StatementNode> parseStatements(boolean isIfStatement, boolean isLoopStatement) {

        List<StatementNode> statements = new ArrayList<>();

        while (!match(Type.EOF) && !(currentTokenIndex >= tokens.size())) {

            if (match(Type.INT) || match(Type.CHAR) || match(Type.FLOAT) || match(Type.BOOL)) {
                error("Found a variable declaration after the executable code", previous());
            }

            if (match(Type.IDENTIFIER)) {

                int counter = 1;
                boolean isAssignment = true;

                while (currentTokenIndex + counter < tokens.size() && peekNext(counter).getType() != Type.NEWLINE) {

                    if (peekNext(counter).getType() == Type.ADD
                            || peekNext(counter).getType() == Type.SUBTRACT
                            || peekNext(counter).getType() == Type.MULTIPLY
                            || peekNext(counter).getType() == Type.DIVIDE
                            || peekNext(counter).getType() == Type.MODULO) {

                        System.out.println("Currnet Token: " + peek());

                        statements.add(parseArithmeticStatement());
                        checkForNewline();
                        isAssignment = false;
                        break;

                    } else if (peekNext(counter).getType() == Type.LESS ||
                            peekNext(counter).getType() == Type.GREATER ||
                            peekNext(counter).getType() == Type.LESS_EQUAL ||
                            peekNext(counter).getType() == Type.GREATER_EQUAL ||
                            peekNext(counter).getType() == Type.NOT_EQUAL ||
                            peekNext(counter).getType() == Type.EQUAL ||
                            peekNext(counter).getType() == Type.AND ||
                            peekNext(counter).getType() == Type.OR ||
                            peekNext(counter).getType() == Type.NOT) {
                        statements.add(parseLogicalStatement());

                        System.out.println("Currnet Token: " + peek());

                        checkForNewline();
                        isAssignment = false;
                        break;
                    }
                    counter++;
                }

                if (isAssignment) {
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

            if (match(Type.FOR)) {
                statements.add(parseForStatement());
                checkForNewline();
                continue;
            }

            if (match(Type.CONTINUE)) {

                if (!isLoopStatement) {
                    error("Continue statement can only be used inside a loop", previous());
                }

                statements.add(new ContinueNode(previous().getPosition()));
                checkForNewline();
                continue;
            }

            if (match(Type.BREAK)) {

                if (!isLoopStatement) {
                    error("Break statement can only be used inside a loop", previous());
                }

                statements.add(new BreakNode(previous().getPosition()));

                checkForNewline();
                continue;
            }

            if (peek().getType() == Type.DEDENT) {
                if (isIfStatement || isLoopStatement) {
                    if (peekNext(1).getType() == Type.END_IF || peekNext(1).getType() == Type.END_WHILE
                            || peekNext(1).getType() == Type.END_FOR) {
                        return statements;
                    } else {
                        error("Invalid Indentation found", peek());
                    }
                } else {
                    if (peekNext(1).getType() == Type.END_CODE) {
                        return statements;
                    } else {
                        error("Invalid Indentation found", peek());
                    }
                }
            }

            if (match(Type.DEDENT)) {
                error("Unexpected DEDENTION", peek());
            }

            if (match(Type.END_CODE)) {
                error("Unexpected END CODE", peek());
            }

            if (match(Type.BEGIN_CODE)) {
                error("Unexpected BEGIN CODE", peek());
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

        if (peekNext(1).getType() != Type.ASSIGNMENT) {

            if (match(Type.IDENTIFIER)) {
                assignments.add(new AssignmentNode(identifier, new VariableNode(previous())));
            } else if (match(Type.LITERAL)) {
                assignments.add(new AssignmentNode(identifier, new LiteralNode(previous())));
            } else {
                error("Expected an identifier or literal after an assignment token.", identifierToken);
            }

        } else {

            System.out.println("Found a chained assignment: " + identifierToken.getLexeme());

            List<Token> variableTokens = new ArrayList<>();
            variableTokens.add(identifierToken);

            do {

                if (peek().getType() != Type.IDENTIFIER && peek().getType() != Type.LITERAL) {
                    error("Expected an identifier or literal after an assigment token.", identifierToken);
                }

                if (match(Type.IDENTIFIER)) {

                    Token var = previous();

                    if (peek().getType() == Type.ASSIGNMENT) {

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

                    System.out.println("\n\n\n Added a Literal: " + var.getLexeme());

                    if (peek().getType() == Type.ASSIGNMENT) {
                        error("Can't assign value to a Literal" + var, var);
                    }

                    for (Token token : variableTokens) {
                        VariableNode left = new VariableNode(token);
                        LiteralNode right = new LiteralNode(var);

                        AssignmentNode assignment = new AssignmentNode(left, right);

                        System.out.println("Assignment: " + assignment);

                        assignments.add(new AssignmentNode(left, right));
                    }

                }
            } while (match(Type.ASSIGNMENT));

        }

        return assignments;
    }

    private AssignmentNode parseArithmeticStatement() {
        Token variableName = previous();

        if (!match(Type.ASSIGNMENT)) {
            error("Invalid arithmetic statement", peek());
            return null;
        }

        VariableNode variable = new VariableNode(variableName);
        ExpressionNode expression = parseExpression();

        return new AssignmentNode(variable, expression);
    }

    private AssignmentNode parseLogicalStatement() {

        Token variableName = previous();

        System.out.println("Variable Name: " + variableName);

        System.out.println("Peek: " + peek());

        if (!match(Type.ASSIGNMENT)) {
            error("Invalid logical statement", peek());
            return null;
        }

        VariableNode variable = new VariableNode(variableName);
        ExpressionNode expression = parseExpression();

        return new AssignmentNode(variable, expression);
    }

    private ExpressionNode parseExpression() {
        return parseLogicalOr();
    }

    private ExpressionNode parseLogicalOr() {
        ExpressionNode left = parseLogicalAnd();

        while (match(Type.OR)) {
            Token operatorToken = previous();
            ExpressionNode right = parseLogicalAnd();
            left = new BinaryNode(left, operatorToken, right);
        }

        return left;
    }

    private ExpressionNode parseLogicalAnd() {
        ExpressionNode left = parseComparisonExpression();

        while (match(Type.AND)) {
            Token operatorToken = previous();
            ExpressionNode right = parseComparisonExpression();
            left = new BinaryNode(left, operatorToken, right);
        }

        return left;
    }

    private ExpressionNode parseComparisonExpression() {
        ExpressionNode left = parseAdditionSubtraction();

        while (match(Type.GREATER) || match(Type.LESS) || match(Type.GREATER_EQUAL) || match(Type.LESS_EQUAL)
                || match(Type.NOT_EQUAL) || match(Type.EQUAL)) {
            Token operatorToken = previous();
            ExpressionNode right = parseAdditionSubtraction();
            left = new BinaryNode(left, operatorToken, right);
        }

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

        } else if (match(Type.POSITIVE) || match(Type.NEGATIVE) || match(Type.NOT)) {
            Token operatorToken = previous();
            ExpressionNode expression = parsePrimary();
            return new UnaryNode(operatorToken, expression);
        } else {
            error("Expect primary expression.", peek());
        }

        return null;
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

            if (match(Type.ESCAPE_CODE_CLOSE)) {

                if (arguments.size() != 0 && arguments.getLast().getType() != Type.CONCATENATION) {
                    error("Can't add another argument without concatention", previous());
                }

                error("Expected escape code open before escape code close", previous());
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
        ExpressionNode ifCondition = parseExpression();

        // Expect ')'
        consume(Type.RIGHT_PARENTHESIS, "Expected ')', after the conditional expression");

        consume(Type.NEWLINE, "Expected new line before the BEGIN IF statement");

        consume(Type.BEGIN_IF, "Expected 'BEGIN IF' after the conditional expression");

        consume(Type.NEWLINE, "Expected new line after the BEGIN IF statement");

        consume(Type.INDENT, "Expected INDENT after the BEGIN IF statement");

        // Parse the body of the if statement
        List<StatementNode> body = parseStatements(true, false);

        consume(Type.DEDENT, "Expected DEDENTION after the body of the if statement");

        consume(Type.END_IF, "Expected 'END IF' after the body of the if statement");

        consume(Type.NEWLINE, "Expected new line after the END IF statement");

        ifStatements.add(new IfNode(ifCondition, body, token.getPosition()));

        while (match(Type.ELSE_IF)) {

            Token elseIfToken = previous();

            consume(Type.LEFT_PARENTHESIS, "Expected '(', after 'ELSE IF' keyword");
            // Parse the conditional expression
            ExpressionNode elseIfCondition = parseExpression();

            // Expect ')'
            consume(Type.RIGHT_PARENTHESIS, "Expected ')', after the conditional expression");

            consume(Type.NEWLINE, "Expected new line after the ELSE IF statement");

            consume(Type.BEGIN_IF, "Expected 'BEGIN ELSE IF' after the conditional expression");

            consume(Type.NEWLINE, "Expected new line after the BEGIN ELSE IF statement");

            consume(Type.INDENT, "Expected INDENT after the BEGIN ELSE IF statement");

            // Parse the body of the if statement
            List<StatementNode> elseIfBody = parseStatements(true, false);

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
            List<StatementNode> elseBody = parseStatements(true, false);

            consume(Type.DEDENT, "Expected DEDENTION after the body of the if statement");

            consume(Type.END_IF, "Expected 'END ELSE' after the body of the if statement");

            consume(Type.NEWLINE, "Expected new line after the END IF statement");

            ifStatements.add(new ElseNode(elseBody, elseToken.getPosition()));
        }

        // Create and return the IfStatementNode
        return ifStatements;
    }

    private StatementNode parseWhileStatement() {

        Token token = previous();

        consume(Type.LEFT_PARENTHESIS, "Expected '(', after 'WHILE' keyword");

        ExpressionNode condition = parseExpression();

        consume(Type.RIGHT_PARENTHESIS, "Expected ')', after the conditional expression");

        consume(Type.NEWLINE, "Expected new line before the BEGIN WHILE statement");

        consume(Type.BEGIN_WHILE, "Expected 'BEGIN WHILE' after the conditional expression");

        consume(Type.NEWLINE, "Expected new line after the BEGIN WHILE statement");

        consume(Type.INDENT, "Expected INDENT after the BEGIN WHILE statement");

        List<StatementNode> body = parseStatements(false, true);

        consume(Type.DEDENT, "Expected DEDENTION after the body of the while statement");

        consume(Type.END_WHILE, "Expected 'END WHILE' after the body of the while statement");

        System.out.println(body);

        return new WhileNode(condition, body, token.getPosition());
    }

    private StatementNode parseForStatement() {
        Token token = previous();

        consume(Type.LEFT_PARENTHESIS, "Expected '(', after 'FOR' keyword");

        AssignmentNode initialization = null;

        if (!match(Type.DELIMITER)) {

            System.out.println("Parsing FOR LOOP initialization");

            match(Type.IDENTIFIER); // Consume the identifier token (variable name

            List<StatementNode> initializations = parseAssignmentStatement();

            if (initializations.size() > 1) {
                error("Expected a single assignment inside the FOR LOOP initialization", token);
            }

            initialization = (AssignmentNode) initializations.get(0);

            System.out.println("Initialization: " + initialization);

            System.out.println("Peek: " + peek());
        }

        consume(Type.DELIMITER, "Expected a SEMI-COLON after the initialization statement");

        ExpressionNode condition = parseExpression();

        consume(Type.DELIMITER, "Expected a SEMI-COLON after the conditional expression");

        consume(Type.IDENTIFIER, "Expected an identifier after the conditional expression");

        AssignmentNode update = parseArithmeticStatement();

        consume(Type.RIGHT_PARENTHESIS, "Expected ')', after the conditional expression");

        consume(Type.NEWLINE, "Expected new line before the BEGIN LOOP statement");

        consume(Type.BEGIN_FOR, "Expected 'BEGIN LOOP' after the conditional expression");

        consume(Type.NEWLINE, "Expected new line after the BEGIN LOOP statement");

        consume(Type.INDENT, "Expected INDENT after the BEGIN LOOP statement");

        List<StatementNode> body = parseStatements(false, true);

        consume(Type.DEDENT, "Expected DEDENTION after the body of the loop statement");

        consume(Type.END_FOR, "Expected 'END LOOP' after the body of the loop statement");

        return new ForNode(initialization, condition, update, body, token.getPosition());
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

        throw new RuntimeException("Syntax error " + ": " + message + " at Line " + token.getPosition().getLine()
                + " and column " + token.getPosition().getColumn());

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