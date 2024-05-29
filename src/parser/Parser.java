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

        consume(Type.BEGIN_CODE, "Expected BEGIN CODE but found " + peek().getLexeme());

        consume(Type.NEWLINE, "Expected NEWLINE AFTER BEGIN CODE but found " + peek().getLexeme());

        if (!match(Type.INDENT)) {

            if (match(Type.END_CODE)) {

                consume(Type.NEWLINE, "Expected NEWLINE AFTER END CODE but found " + peek().getLexeme());

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

            error("Expected INDENTION AFTER BEGIN CODE but found " + peek().getLexeme(), peek());
        }

        parseDeclaration();

        consume(Type.DEDENT, "Expected DEDENTION IN END CODE but found " + peek().getLexeme());
        consume(Type.END_CODE, "Expected END CODE but found " + peek().getLexeme());
        consume(Type.NEWLINE, "Expected NEWLINE AFTER END CODE but found " + peek().getLexeme());

        while (!match(Type.EOF)) {
            if (!match(Type.NEWLINE)) {
                error("Code should be inside 'BEGIN CODE' and 'END CODE' markers. Found code outside this range.",
                        peek());
            }
        }

        if (!isAtEnd()) {
            error("Code should be inside the BEGIN CODE AND END CODE. Found code outside this range.", peek());
        }

        return new ProgramNode(declarations, programStatements);
    }

    private void parseDeclaration() {

        while (match(Type.INT) || match(Type.CHAR) || match(Type.FLOAT) || match(Type.BOOL)) {
            declarations.addAll(parseVariableDeclaration());
        }

        if (declarations.size() == 0) {
            error("Invalid Data Type Detected", peek());
        }
        programStatements.addAll(parseStatements(false, false));
    }

    private List<VariableDeclarationNode> parseVariableDeclaration() {
        Token dataType = previous();
        List<VariableDeclarationNode> variables = new ArrayList<>();

        do {
            Token identifier = consume(Type.IDENTIFIER, "Expected an identifier but got a/an" + peek().getType());

            if (match(Type.ASSIGNMENT)) {
                Token unary = null;
                if (match(Type.NEGATIVE) || match(Type.POSITIVE)) {
                    unary = previous();
                    Token literal = consume(Type.LITERAL, "Expected literal after assignment");

                    variables.add(new VariableDeclarationNode(dataType, identifier,
                            new Token(Type.LITERAL, unary.getLexeme() + literal.getLexeme(), null)));
                } else {
                    Token literal = consume(Type.LITERAL, "Expected literal after assignment");

                    if (!literal.getLexeme().matches("[0-9]+") &&
                            !literal.getLexeme().equalsIgnoreCase("TRUE") &&
                            !literal.getLexeme().equalsIgnoreCase("FALSE") &&
                            !literal.getLexeme().matches("[0-9]*\\.?[0-9]+")) {

                        if (literal.getLexeme().length() > 1) {
                            error("Invalid character literal", literal);
                        }
                    }

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

            System.out.println("Current Token: " + peek());

            if (match(Type.INT) || match(Type.CHAR) || match(Type.FLOAT) || match(Type.BOOL)) {
                error("Found a variable declaration in the executable code", previous());
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

                        statements.add(parseArithmeticStatement());
                        consume(Type.NEWLINE,
                                "Expected a newline after the statement. Please ensure each statement is on its own line.");

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

                        consume(Type.NEWLINE,
                                "Expected a newline after the statement. Please ensure each statement is on its own line.");

                        isAssignment = false;
                        break;
                    }
                    counter++;
                }

                if (isAssignment) {
                    statements.addAll(parseAssignmentStatement());
                    consume(Type.NEWLINE,
                            "Expected a newline after the statement. Please ensure each statement is on its own line.");

                }

                continue;
            }

            if (match(Type.DISPLAY)) {
                statements.add(parseDisplayStatement());
                consume(Type.NEWLINE,
                        "Expected a newline after the statement. Please ensure each statement is on its own line.");

                continue;
            }

            if (match(Type.SCAN)) {
                statements.add(parseScanStatement());
                consume(Type.NEWLINE,
                        "Expected a newline after the statement. Please ensure each statement is on its own line.");

                continue;
            }

            if (match(Type.IF)) {
                statements.addAll(parseIfStatement());
                continue;
            }

            if (match(Type.WHILE)) {
                statements.add(parseWhileStatement());
                consume(Type.NEWLINE,
                        "Expected a newline after the statement. Please ensure each statement is on its own line.");
                continue;
            }

            if (match(Type.ELSE_IF)) {
                error("Found an ELSE IF statement without an IF statement", previous());
            }

            if (match(Type.ELSE)) {
                error("Found an ELSE statement without an IF statement", previous());
            }

            if (match(Type.FOR)) {
                statements.add(parseForStatement());
                consume(Type.NEWLINE,
                        "Expected a newline after the statement. Please ensure each statement is on its own line.");
                continue;
            }

            if (match(Type.CONTINUE)) {

                if (!isLoopStatement) {
                    error("Continue statement can only be used inside a loop", previous());
                }

                statements.add(new ContinueNode(previous().getPosition()));
                consume(Type.NEWLINE,
                        "Expected a newline after the statement. Please ensure each statement is on its own line.");

                continue;
            }

            if (match(Type.BREAK)) {

                if (!isLoopStatement) {
                    error("Break statement can only be used inside a loop", previous());
                }

                statements.add(new BreakNode(previous().getPosition()));

                consume(Type.NEWLINE,
                        "Expected a newline after the statement. Please ensure each statement is on its own line.");

                continue;
            }

            if (peek().getType() == Type.DEDENT) {
                if (isIfStatement || isLoopStatement) {
                    if (peekNext(1).getType() == Type.END_IF || peekNext(1).getType() == Type.END_WHILE
                            || peekNext(1).getType() == Type.END_FOR) {
                        return statements;
                    } else {
                        error("Invalid indentation found", peek());
                    }
                } else {
                    if (peekNext(1).getType() == Type.END_CODE) {
                        return statements;
                    } else {
                        error("Invalid indentation found", peek());
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

        consume(Type.ASSIGNMENT,
                "While parsing an assignment statement, expected assignment '=' but got '" + peek().getType()
                        + "'");

        if (peekNext(1).getType() != Type.ASSIGNMENT) {

            if (match(Type.IDENTIFIER)) {
                assignments.add(new AssignmentNode(identifier, new VariableNode(previous())));
            } else if (match(Type.LITERAL)) {
                assignments.add(new AssignmentNode(identifier, new LiteralNode(previous())));
            } else if (match(Type.NEGATIVE) || match(Type.POSITIVE) || match(Type.NOT)) {

                Token unary = previous();

                if (match(Type.IDENTIFIER)) {
                    assignments.add(new AssignmentNode(identifier, new UnaryNode(unary, new VariableNode(previous()))));
                } else if (match(Type.LITERAL)) {
                    assignments.add(new AssignmentNode(identifier, new UnaryNode(unary, new LiteralNode(previous()))));
                } else {
                    error("Expected an identifier or literal after a unary operator", identifierToken);
                }
            }

        } else {

            List<Token> variableTokens = new ArrayList<>();
            variableTokens.add(identifierToken);

            do {

                if (peek().getType() != Type.IDENTIFIER && peek().getType() != Type.LITERAL
                        && peek().getType() != Type.NEGATIVE && peek().getType() != Type.POSITIVE
                        && peek().getType() != Type.NOT) {
                    error("Expected an identifier or literal or unary operator after an assigment '=' operator.",
                            identifierToken);
                }

                if (match(Type.IDENTIFIER)) {

                    Token var = previous();

                    if (peek().getType() == Type.ASSIGNMENT) {

                        variableTokens.add(var);

                    } else {

                        for (Token token : variableTokens) {

                            VariableNode left = new VariableNode(token);
                            VariableNode right = new VariableNode(var);

                            assignments.add(new AssignmentNode(left, right));
                        }
                    }

                } else if (match(Type.LITERAL)) {

                    Token var = previous();

                    if (peek().getType() == Type.ASSIGNMENT) {
                        error("Can't assign value to a Literal.", var);
                    }

                    for (Token token : variableTokens) {
                        VariableNode left = new VariableNode(token);
                        LiteralNode right = new LiteralNode(var);

                        AssignmentNode assignment = new AssignmentNode(left, right);

                        assignments.add(new AssignmentNode(left, right));
                    }

                } else if (match(Type.NOT) || match(Type.POSITIVE) || match(Type.NEGATIVE)) {

                    Token UnaryOperator = previous();

                    if (peek().getType() == Type.ASSIGNMENT) {
                        error("Can't assign value to a NOT operator.", UnaryOperator);
                    }

                    if (match(Type.IDENTIFIER)) {

                        Token unaryOperand = previous();

                        for (Token token : variableTokens) {

                            VariableNode left = new VariableNode(token);
                            UnaryNode right = new UnaryNode(UnaryOperator, new VariableNode(unaryOperand));

                            assignments.add(new AssignmentNode(left, right));
                        }
                    } else if (match(Type.LITERAL)) {

                        Token unaryOperand = previous();

                        for (Token token : variableTokens) {
                            VariableNode left = new VariableNode(token);
                            UnaryNode right = new UnaryNode(UnaryOperator, new LiteralNode(unaryOperand));

                            assignments.add(new AssignmentNode(left, right));
                        }
                    } else {
                        error("Expected an identifier or literal after a unary operator", identifierToken);

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
            error("Expected an expression but got " + peek().getType() + ".", peek());
        }

        return null;
    }

    private StatementNode parseDisplayStatement() {

        consume(Type.COLON, "Expected colon after Display Call");
        List<Token> arguments = new ArrayList<>();
        List<ExpressionNode> expressions = new ArrayList<ExpressionNode>();

        while (peek().getType() != Type.NEWLINE) {

            if (match(Type.CONCATENATION)) {

                if (arguments.size() == 0) {
                    error("Missing argument before concatenation", previous());
                }

                if (arguments.getLast().getType() == Type.CONCATENATION) {
                    error("Can't add another concatenation after a concatenation", previous());
                }

                arguments.add(previous());
            }

            else if (match(Type.IDENTIFIER)) {

                if (arguments.size() != 0 && arguments.getLast().getType() != Type.CONCATENATION &&
                        arguments.getLast().getType() != Type.ADD &&
                        arguments.getLast().getType() != Type.SUBTRACT &&
                        arguments.getLast().getType() != Type.MULTIPLY &&
                        arguments.getLast().getType() != Type.DIVIDE &&
                        arguments.getLast().getType() != Type.MODULO &&
                        arguments.getLast().getType() != Type.AND &&
                        arguments.getLast().getType() != Type.OR &&
                        arguments.getLast().getType() != Type.NOT) {
                    error("Can't add another argument without concatenation", previous());
                }

                arguments.add(previous());
            }

            else if (match(Type.ESCAPE_CODE_OPEN)) {

                if (arguments.size() != 0 && arguments.getLast().getType() != Type.CONCATENATION) {
                    error("Can't add another argument without concatenation", previous());
                }

                arguments.add(consume(Type.SPECIAL_CHARACTER, "Expected special character after escape code open"));
                consume(Type.ESCAPE_CODE_CLOSE, "Expected escape code close");
            }

            else if (match(Type.DELIMITER)) {

                if (arguments.size() != 0 && arguments.getLast().getType() != Type.CONCATENATION) {
                    error("Can't add another argument without concatention", previous());
                }

                arguments.add(consume(Type.STRING_LITERAL, "Expected string literal after delimiter"));

                consume(Type.DELIMITER, "Expected closing delimiter after the string literal");
            }

            else if (match(Type.ESCAPE_CODE_CLOSE)) {

                if (arguments.size() != 0 && arguments.getLast().getType() != Type.CONCATENATION) {
                    error("Can't add another argument without concatention", previous());
                }

                error("Expected escape code open before escape code close", previous());
            }

            else if (match(Type.LITERAL)) {

                if (arguments.size() != 0) {

                    Type prev = arguments.getLast().getType();

                    if (prev != Type.CONCATENATION &&
                            prev != Type.ADD &&
                            prev != Type.SUBTRACT &&
                            prev != Type.MULTIPLY &&
                            prev != Type.DIVIDE &&
                            prev != Type.MODULO &&
                            prev != Type.GREATER &&
                            prev != Type.LESS &&
                            prev != Type.GREATER_EQUAL &&
                            prev != Type.LESS_EQUAL &&
                            prev != Type.NOT_EQUAL &&
                            prev != Type.EQUAL &&
                            prev != Type.AND &&
                            prev != Type.OR) {
                        error("Can't add another number literal without concatenation or arithmetic operation",
                                previous());
                    }
                }

                Token literal = previous();

                if (!literal.getLexeme().matches("[0-9]+") &&
                        !literal.getLexeme().equalsIgnoreCase("TRUE") &&
                        !literal.getLexeme().equalsIgnoreCase("FALSE") &&
                        !literal.getLexeme().matches("[0-9]*\\.?[0-9]+")) {

                    if (literal.getLexeme().length() > 1) {
                        error("Invalid character literal", literal);
                    }
                }

                arguments.add(previous());
            }

            else if (peek().getType() == Type.ADD || peek().getType() == Type.SUBTRACT ||
                    peek().getType() == Type.MULTIPLY || peek().getType() == Type.DIVIDE ||
                    peek().getType() == Type.MODULO) {

                if (arguments.size() == 0 && arguments.getLast().getType() != Type.LITERAL
                        && arguments.getLast().getType() != Type.IDENTIFIER) {
                    error("Can't perform arithmetic operation without a left-side identifier or number literal",
                            previous());
                }

                currentTokenIndex--;
                arguments.remove(arguments.size() - 1);
                ExpressionNode expression = parseExpression();
                expressions.add(expression);

                StringBuilder sb = new StringBuilder();
                for (Token token : expression.getTokens()) {
                    sb.append(token.getLexeme() + " ");
                }

                arguments.add(new Token(Type.EXPRESSION, sb.toString(),
                        expression.getToken(expression.countTokens() - 1).getPosition()));
            }

            else if (peek().getType() == Type.GREATER || peek().getType() == Type.LESS ||
                    peek().getType() == Type.GREATER_EQUAL || peek().getType() == Type.LESS_EQUAL ||
                    peek().getType() == Type.NOT_EQUAL || peek().getType() == Type.EQUAL) {

                if (arguments.size() == 0 && arguments.getLast().getType() != Type.LITERAL
                        && arguments.getLast().getType() != Type.IDENTIFIER) {
                    error("Can't perform logical operation without a left-side identifier or number literal",
                            previous());
                }

                if (arguments.getLast().getType() == Type.STRING_LITERAL) {
                    error("Operation on string literals are not supported", previous());
                }

                currentTokenIndex--;
                arguments.remove(arguments.size() - 1);
                ExpressionNode expression = parseExpression();
                expressions.add(expression);

                StringBuilder sb = new StringBuilder();
                for (Token token : expression.getTokens()) {
                    sb.append(token.getLexeme() + " ");
                }

                arguments.add(new Token(Type.EXPRESSION, sb.toString(),
                        expression.getToken(expression.countTokens() - 1).getPosition()));
            }

            else if (match(Type.NOT)) {

                if (arguments.size() != 0 && arguments.getLast().getType() != Type.CONCATENATION) {
                    error("Can't add another argument without concatenation", previous());
                }

                Token notToken = previous();

                ExpressionNode operand = null;

                if (match(Type.IDENTIFIER)) {
                    operand = new VariableNode(previous());
                } else if (match(Type.LITERAL)) {
                    operand = new LiteralNode(previous());
                } else {
                    error("Expected an identifier or literal after NOT operator", previous());
                }

                ExpressionNode expression = new UnaryNode(notToken, operand);
                expressions.add(expression);

                arguments.add(new Token(Type.EXPRESSION, expression.toString(),
                        expression.getToken(expression.countTokens() - 1).getPosition()));
            }

            else if (match(Type.STRING_LITERAL) || match(Type.SPECIAL_CHARACTER) || match(Type.COLON)
                    || match(Type.NEXT_LINE)) {
                if (arguments.size() != 0 && arguments.getLast().getType() != Type.CONCATENATION) {
                    error("Can't add another argument without concatenation", previous());
                }

                arguments.add(previous());
            }
        }

        if (previous().getType() == Type.CONCATENATION) {
            error("Missing argument after concatenation symbol", previous());
        }

        return new DisplayNode(arguments, expressions);
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

        consume(Type.LEFT_PARENTHESIS, "Expected '(', after 'IF' keyword but got " + peek().getLexeme());
        // Parse the conditional expression
        ExpressionNode ifCondition = parseExpression();

        consume(Type.RIGHT_PARENTHESIS, "Expected ')', after the conditional expression but got " + peek().getLexeme());
        consume(Type.NEWLINE, "Expected new line before the BEGIN IF statement but got " + peek().getLexeme());
        consume(Type.BEGIN_IF, "Expected 'BEGIN IF' after the conditional expression but got " + peek().getLexeme());
        consume(Type.NEWLINE, "Expected new line after the BEGIN IF statement but got " + peek().getLexeme());
        consume(Type.INDENT, "Expected INDENT after the BEGIN IF statement but got " + peek().getLexeme());

        // Parse the body of the if statement
        List<StatementNode> body = parseStatements(true, false);

        consume(Type.DEDENT, "Expected DEDENTION after the body of the if statement but got " + peek().getLexeme());
        consume(Type.END_IF, "Expected 'END IF' after the body of the if statement but got " + peek().getLexeme());
        consume(Type.NEWLINE, "Expected new line after the END IF statement but got " + peek().getLexeme());

        ifStatements.add(new IfNode(ifCondition, body, token.getPosition()));

        while (match(Type.ELSE_IF)) {

            Token ifElseToken = previous();

            consume(Type.LEFT_PARENTHESIS, "Expected a '(', after 'ELSE IF' keyword but got " + peek().getLexeme());
            // Parse the conditional expression
            ExpressionNode ifElseCondition = parseExpression();

            // Expect ')'
            consume(Type.RIGHT_PARENTHESIS,
                    "Expected a ')', after the conditional expression but got " + peek().getLexeme());
            consume(Type.NEWLINE, "Expected a new line after the ELSE IF statement but got " + peek().getLexeme());
            consume(Type.BEGIN_IF,
                    "Expected a 'BEGIN IF' after the conditional expression but got " + peek().getLexeme());
            consume(Type.NEWLINE, "Expected a NEW LINE after the BEGIN IF statement but got " + peek().getLexeme());
            consume(Type.INDENT, "Expected an INDENTION after the BEGIN IF statement but got " + peek().getLexeme());

            // Parse the body of the if statement
            List<StatementNode> ifElseBody = parseStatements(true, false);

            consume(Type.DEDENT,
                    "Expected DEDENTION after the body of the IF ELSE statement but got " + peek().getLexeme());
            consume(Type.END_IF,
                    "Expected an 'END ELSE' after the body of the IF ELSE statement but got " + peek().getLexeme());
            consume(Type.NEWLINE, "Expected a new line after the END IF statement but got " + peek().getLexeme());

            ifStatements.add(new ElseIfNode(ifElseCondition, ifElseBody, ifElseToken.getPosition()));
        }

        if (match(Type.ELSE)) {

            Token elseToken = previous();

            consume(Type.NEWLINE, "Expected a new line after the END IF statement but got " + peek().getLexeme());
            consume(Type.BEGIN_IF,
                    "Expected a 'BEGIN ELSE' after the conditional expression but got " + peek().getLexeme());
            consume(Type.NEWLINE, "Expected a new line after the BEGIN ELSE statement but got " + peek().getLexeme());
            consume(Type.INDENT, "Expected an INDENTION after the BEGIN ELSE statement but got " + peek().getLexeme());

            // Parse the body of the if statement
            List<StatementNode> elseBody = parseStatements(true, false);

            consume(Type.DEDENT,
                    "Expected a DEDENTION after the body of the if statement but got " + peek().getLexeme());
            consume(Type.END_IF,
                    "Expected an 'END ELSE' after the body of the if statement but got " + peek().getLexeme());
            consume(Type.NEWLINE, "Expected a new line after the END IF statement but got " + peek().getLexeme());

            ifStatements.add(new ElseNode(elseBody, elseToken.getPosition()));
        }

        // Create and return the IfStatementNode
        return ifStatements;
    }

    private StatementNode parseWhileStatement() {

        Token token = previous();

        consume(Type.LEFT_PARENTHESIS, "Expected '(', after 'WHILE' keyword but got " + peek().getLexeme());

        ExpressionNode condition = parseExpression();

        consume(Type.RIGHT_PARENTHESIS, "Expected ')', after the conditional expression but got " + peek().getLexeme());
        consume(Type.NEWLINE, "Expected new line before the BEGIN WHILE statement but got " + peek().getLexeme());
        consume(Type.BEGIN_WHILE,
                "Expected 'BEGIN WHILE' after the conditional expression but got " + peek().getLexeme());
        consume(Type.NEWLINE, "Expected new line after the BEGIN WHILE statement but got " + peek().getLexeme());
        consume(Type.INDENT, "Expected INDENT after the BEGIN WHILE statement but got " + peek().getLexeme());

        List<StatementNode> body = parseStatements(false, true);

        consume(Type.DEDENT, "Expected DEDENTION after the body of the while statement but got " + peek().getLexeme());
        consume(Type.END_WHILE,
                "Expected 'END WHILE' after the body of the while statement but got " + peek().getLexeme());

        return new WhileNode(condition, body, token.getPosition());
    }

    private StatementNode parseForStatement() {
        Token token = previous();

        consume(Type.LEFT_PARENTHESIS, "Expected '(', after 'FOR' keyword");

        AssignmentNode initialization = null;

        if (peek().getType() != Type.DELIMITER) {

            if (match(Type.INT) || match(Type.CHAR) || match(Type.FLOAT) || match(Type.BOOL)) {
                error("Variable declaration inside the for loop is not allowed. It should be at the start of the begin code.",
                        previous());
            }

            match(Type.IDENTIFIER); // Consume the identifier token (variable name)

            List<StatementNode> initializations = parseAssignmentStatement();

            if (initializations.size() > 1) {
                error("Expected a single assignment inside the FOR LOOP initialization", token);
            }

            initialization = (AssignmentNode) initializations.get(0);
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

    private void error(String message, Token token) {
        System.err.println("Syntax error: " + message + " at Line " +
                token.getPosition().getLine()
                + " and Column " + token.getPosition().getColumn() + "\n");
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