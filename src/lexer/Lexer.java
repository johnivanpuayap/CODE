package src.lexer;

import java.util.List;
import java.util.ArrayList;
import src.utils.Position;
import src.utils.Token;
import src.utils.Type;

public class Lexer {
    private final String input;
    private Position position;
    private int counter;
    private int indentLevel;
    private List<Token> tokens = new ArrayList<>();

    public Lexer(String input) {
        this.input = input;
        this.position = new Position(1, 1);
        this.counter = 0;
        this.indentLevel = 0;
    }

    public List<Token> tokenize() {

        while (counter < input.length()) {
            char currentChar = input.charAt(counter);
            if (input.startsWith("BEGIN CODE", counter)) {
                tokens.add(new Token(Type.BEGIN_CODE, "BEGIN CODE",
                        new Position(position.getLine(), position.getColumn())));
                position.add("BEGIN CODE".length());
                counter += "BEGIN CODE".length();
            } else if (input.startsWith("END CODE", counter)) {
                tokens.add(
                        new Token(Type.END_CODE, "END CODE", new Position(position.getLine(), position.getColumn())));
                position.add("END CODE".length());
                counter += "END CODE".length();
            } else if (input.startsWith("DISPLAY", counter)) {
                tokens.add(new Token(Type.DISPLAY, "DISPLAY", new Position(position.getLine(), position.getColumn())));
                position.add("DISPLAY".length());
                counter += "DISPLAY".length();
                tokens = tokenizeDisplay(tokens);
            } else if (input.startsWith("SCAN", counter)) {
                tokens.add(new Token(Type.SCAN, "SCAN", new Position(position.getLine(), position.getColumn())));
                position.add("SCAN".length());
                counter += "SCAN".length();
            } else if (input.startsWith("AND", counter)) {
                tokens.add(new Token(Type.AND, "AND", new Position(position.getLine(), position.getColumn())));
                position.add("AND".length());
                counter += "AND".length();
            } else if (input.startsWith("OR", counter)) {
                tokens.add(new Token(Type.OR, "OR", new Position(position.getLine(), position.getColumn())));
                position.add("OR".length());
                counter += "OR".length();
            } else if (input.startsWith("NOT", counter)) {
                tokens.add(new Token(Type.NOT, "NOT", new Position(position.getLine(), position.getColumn())));
                position.add("NOT".length());
                counter += "NOT".length();
            } else if (input.startsWith("IF", counter)) {
                tokens.add(new Token(Type.IF, "IF", new Position(position.getLine(), position.getColumn())));
                position.add("IF".length());
                counter += "IF".length();
            } else if (input.startsWith("ELSE IF", counter)) {
                tokens.add(new Token(Type.ELSE_IF, "ELSE IF", new Position(position.getLine(), position.getColumn())));
                position.add("ELSE IF".length());
                counter += "ELSE IF".length();
            } else if (input.startsWith("ELSE", counter)) {
                tokens.add(new Token(Type.ELSE, "ELSE", new Position(position.getLine(), position.getColumn())));
                position.add("ELSE".length());
                counter += "ELSE".length();
            } else if (input.startsWith("BEGIN IF", counter)) {
                tokens.add(
                        new Token(Type.BEGIN_IF, "BEGIN IF", new Position(position.getLine(), position.getColumn())));
                position.add("BEGIN IF".length());
                counter += "BEGIN IF".length();
            } else if (input.startsWith("END IF", counter)) {
                tokens.add(new Token(Type.END_IF, "END IF", new Position(position.getLine(), position.getColumn())));
                position.add("END IF".length());
                counter += "END IF".length();
            } else if (input.startsWith("WHILE", counter)) {
                tokens.add(new Token(Type.WHILE, "WHILE", new Position(position.getLine(), position.getColumn())));
                position.add("WHILE".length());
                counter += "WHILE".length();
            } else if (input.startsWith("BEGIN WHILE", counter)) {
                tokens.add(new Token(Type.BEGIN_WHILE, "BEGIN WHILE",
                        new Position(position.getLine(), position.getColumn())));
                position.add("BEGIN WHILE".length());
                counter += "BEGIN WHILE".length();
            } else if (input.startsWith("END WHILE", counter)) {
                tokens.add(
                        new Token(Type.END_WHILE, "END WHILE", new Position(position.getLine(), position.getColumn())));
                position.add("END WHILE".length());
                counter += "END WHILE".length();
            } else if (input.startsWith("FOR", counter)) {
                tokens.add(new Token(Type.FOR, "FOR", new Position(position.getLine(), position.getColumn())));
                position.add("FOR".length());
                counter += "FOR".length();
            } else if (input.startsWith("BEGIN FOR", counter)) {
                tokens.add(new Token(Type.BEGIN_FOR, "BEGIN FOR",
                        new Position(position.getLine(), position.getColumn())));
                position.add("BEGIN FOR".length());
                counter += "BEGIN FOR".length();
            } else if (input.startsWith("END FOR", counter)) {
                tokens.add(
                        new Token(Type.END_FOR, "END FOR", new Position(position.getLine(), position.getColumn())));
                position.add("END FOR".length());
                counter += "END FOR".length();
            } else if (input.startsWith("CONTINUE", counter)) {
                tokens.add(
                        new Token(Type.CONTINUE, "CONTINUE", new Position(position.getLine(), position.getColumn())));
                position.add("CONTINUE".length());
                counter += "CONTINUE".length();
            } else if (input.startsWith("BREAK", counter)) {
                tokens.add(new Token(Type.BREAK, "BREAK", new Position(position.getLine(), position.getColumn())));
                position.add("BREAK".length());
                counter += "BREAK".length();
            } else if (currentChar == '=') {
                if (input.charAt(counter + 1) == '=') {
                    tokens.add(new Token(Type.EQUAL, "==", new Position(position.getLine(), position.getColumn())));
                    position.add(2);
                    counter += 2;
                } else {
                    tokens.add(new Token(Type.ASSIGNMENT, "=", new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter++;
                }
            } else if (currentChar == ':') {
                tokens.add(new Token(Type.COLON, ":", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (currentChar == ',') {
                tokens.add(new Token(Type.COMMA, ",", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (currentChar == '\'') {
                tokens.add(tokenizeLiteral());
            } else if (currentChar == '\"') {
                tokens.add(tokenizeLiteral());
            } else if (currentChar == '&') {
                tokens.add(new Token(Type.CONCATENATION, "$", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (currentChar == '$') {
                tokens.add(new Token(Type.NEXT_LINE, "$", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (currentChar == '(') {
                tokens.add(
                        new Token(Type.LEFT_PARENTHESIS, "(", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (currentChar == ')') {
                tokens.add(
                        new Token(Type.RIGHT_PARENTHESIS, ")", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (currentChar == '[') {
                tokens.add(
                        new Token(Type.ESCAPE_CODE_OPEN, ")", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (currentChar == '[') {
                tokens.add(
                        new Token(Type.ESCAPE_CODE_CLOSE, ")", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (currentChar == '+') {
                Token latest_token = tokens.get(tokens.size() - 1);
                if (latest_token.getType() == Type.ADD ||
                        latest_token.getType() == Type.SUBTRACT ||
                        latest_token.getType() == Type.MULTIPLY ||
                        latest_token.getType() == Type.DIVIDE ||
                        latest_token.getType() == Type.MODULO ||
                        latest_token.getType() == Type.GREATER ||
                        latest_token.getType() == Type.LESS ||
                        latest_token.getType() == Type.ASSIGNMENT ||
                        latest_token.getType() == Type.EQUAL ||
                        latest_token.getType() == Type.GREATER_EQUAL ||
                        latest_token.getType() == Type.LESS_EQUAL ||
                        latest_token.getType() == Type.NOT_EQUAL ||
                        latest_token.getType() == Type.AND ||
                        latest_token.getType() == Type.OR ||
                        latest_token.getType() == Type.NOT ||
                        latest_token.getType() == Type.NEGATIVE ||
                        latest_token.getType() == Type.POSITIVE ||
                        latest_token.getType() == Type.LEFT_PARENTHESIS) {
                    tokens.add(new Token(Type.POSITIVE, "+", new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter++;
                } else {
                    tokens.add(new Token(Type.ADD, "+", new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter++;
                }
            } else if (currentChar == '-') {
                Token latest_token = tokens.get(tokens.size() - 1);
                if (latest_token.getType() == Type.ADD ||
                        latest_token.getType() == Type.SUBTRACT ||
                        latest_token.getType() == Type.MULTIPLY ||
                        latest_token.getType() == Type.DIVIDE ||
                        latest_token.getType() == Type.GREATER ||
                        latest_token.getType() == Type.LESS ||
                        latest_token.getType() == Type.ASSIGNMENT ||
                        latest_token.getType() == Type.EQUAL ||
                        latest_token.getType() == Type.GREATER_EQUAL ||
                        latest_token.getType() == Type.LESS_EQUAL ||
                        latest_token.getType() == Type.NOT_EQUAL ||
                        latest_token.getType() == Type.AND ||
                        latest_token.getType() == Type.OR ||
                        latest_token.getType() == Type.NOT ||
                        latest_token.getType() == Type.NEGATIVE ||
                        latest_token.getType() == Type.POSITIVE ||
                        latest_token.getType() == Type.LEFT_PARENTHESIS) {
                    tokens.add(new Token(Type.NEGATIVE, "-", new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter++;
                } else {
                    tokens.add(new Token(Type.SUBTRACT, "-", new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter++;
                }
            } else if (currentChar == '*') {
                tokens.add(new Token(Type.MULTIPLY, "*", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (currentChar == '/') {
                tokens.add(new Token(Type.DIVIDE, "/", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (currentChar == '%') {
                tokens.add(new Token(Type.MODULO, "%", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (currentChar == '>') {
                if (input.charAt(counter + 1) == '=') {
                    tokens.add(new Token(Type.GREATER_EQUAL, ">=",
                            new Position(position.getLine(), position.getColumn())));
                    position.add(2);
                    counter += 2;
                } else {
                    tokens.add(new Token(Type.GREATER, ">", new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter++;
                }
            } else if (currentChar == '<') {
                if (input.charAt(counter + 1) == '=') {
                    tokens.add(
                            new Token(Type.LESS_EQUAL, "<=", new Position(position.getLine(), position.getColumn())));
                    position.add(2);
                    counter += 2;
                } else if (input.charAt(counter + 1) == '>') {
                    tokens.add(new Token(Type.NOT_EQUAL, "<>", new Position(position.getLine(), position.getColumn())));
                    position.add(2);
                    counter += 2;
                } else {
                    tokens.add(new Token(Type.LESS, "<", new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter++;
                }

            } else if (currentChar == '#') {

                // Skip comments

                while (counter < input.length() && input.charAt(counter) != '\n') {
                    counter++;
                    position.add(1);
                }

                counter++;
                position.newLine();

                List<Token> indents = checkIndentLevel(position);

                if (indents != null) {
                    tokens.addAll(indents);
                }
            } else if (currentChar == ';') {
                tokens.add(new Token(Type.DELIMITER, ";", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (currentChar == '\n') {

                if (input.charAt(counter - 1) == '\n' || tokens.get(tokens.size() - 1).getType() == Type.NEWLINE) {
                    counter++;
                    position.newLine();
                } else {
                    tokens.add(new Token(Type.NEWLINE, "\n", new Position(position.getLine(), position.getColumn())));
                    counter++;
                    position.newLine();
                }

                if (counter + 1 < input.length() && (input.charAt(counter) != '\n')) {
                    List<Token> indents = checkIndentLevel(position);

                    if (indents != null) {
                        tokens.addAll(indents);
                    }
                }

            }

            else if (Character.isLetter(currentChar) || currentChar == '_') {
                tokens.add(tokenizeIdentifier());
            } else if (Character.isDigit(currentChar)) {
                tokens.add(tokenizeLiteral());
            } else if (Character.isWhitespace(currentChar)) {
                position.add(1);
                counter++;
            } else {
                System.err.println("Lexer Error: Invalid character found: " + currentChar
                        + new Position(position.getLine(), position.getColumn()));
                System.exit(1);
            }
        }

        tokens.add(new Token(Type.EOF, "", new Position(position.getLine(), position.getColumn())));
        return tokens;
    }

    private Token tokenizeIdentifier() {

        StringBuilder identifier = new StringBuilder();

        while (counter < input.length()
                && (Character.isLetterOrDigit(input.charAt(counter)) || input.charAt(counter) == '_')) {
            identifier.append(input.charAt(counter));
            position.add(1);
            counter++;
        }

        String identifierStr = identifier.toString();

        if (identifierStr.equals("INT")) {
            return new Token(Type.INT, "INT", new Position(position.getLine(), position.getColumn()));
        } else if (identifierStr.equals("CHAR")) {
            return new Token(Type.CHAR, "CHAR", new Position(position.getLine(), position.getColumn()));
        } else if (identifierStr.equals("FLOAT")) {
            return new Token(Type.FLOAT, "FLOAT", new Position(position.getLine(), position.getColumn()));
        } else if (identifierStr.equals("BOOL")) {
            return new Token(Type.BOOL, "BOOL", new Position(position.getLine(), position.getColumn()));
        } else {
            return new Token(Type.IDENTIFIER, identifierStr, new Position(position.getLine(), position.getColumn()));
        }
    }

    private Token tokenizeLiteral() {

        StringBuilder literal = new StringBuilder();

        while (counter < input.length() && input.charAt(counter) != '\n' && input.charAt(counter) != ' '
                && input.charAt(counter) != ',' && input.charAt(counter) != ')' && input.charAt(counter) != '('
                && input.charAt(counter) != '=' && input.charAt(counter) != ';' && input.charAt(counter) != '/'
                && input.charAt(counter) != '*' && input.charAt(counter) != '+' && input.charAt(counter) != '-'
                && input.charAt(counter) != '%' && input.charAt(counter) != '<' && input.charAt(counter) != '>'
                && input.charAt(counter) != '&' && input.charAt(counter) != '|' && input.charAt(counter) != '!') {
            literal.append(input.charAt(counter));
            counter++;
            position.add(1);
        }

        if (tokens.getLast().getType() == Type.COMMA || tokens.getLast().getType() == Type.CHAR
                || tokens.getLast().getType() == Type.INT || tokens.getLast().getType() == Type.FLOAT
                || tokens.getLast().getType() == Type.BOOL) {

            return new Token(Type.IDENTIFIER, literal.toString(),
                    new Position(position.getLine(), position.getColumn()));
        }

        System.out.println("Literal: " + literal.toString());

        if (literal.toString().equals("\"TRUE\"")) {
            return new Token(Type.LITERAL, "TRUE",
                    new Position(position.getLine(), position.getColumn()));
        }

        if ((literal.toString().equals("\"FALSE\""))) {
            return new Token(Type.LITERAL, "FALSE",
                    new Position(position.getLine(), position.getColumn()));
        }

        if (literal.toString().startsWith("\"") && literal.toString().endsWith("\"")) {
            return new Token(Type.STRING_LITERAL, literal.toString(),
                    new Position(position.getLine(), position.getColumn()));
        }

        if (literal.toString().startsWith("\'") && literal.toString().endsWith("\'")) {
            String literalWithoutQuotes = literal.toString().substring(1, literal.toString().length() - 1);
            return new Token(Type.LITERAL, literalWithoutQuotes,
                    new Position(position.getLine(), position.getColumn()));
        }

        return new Token(Type.LITERAL, literal.toString(), new Position(position.getLine(), position.getColumn()));
    }

    private List<Token> tokenizeDisplay(List<Token> tokens) {

        // Parse the display string
        if (input.charAt(counter) == ':') {
            tokens.add(new Token(Type.COLON, ":", new Position(position.getLine(),
                    position.getColumn())));
            position.add(1);
            counter++;
        }

        // Skip trailing whitespace
        while (counter < input.length() && input.charAt(counter) == ' ') {
            position.add(1);
            counter++;
        }

        // Parse the concatenated string and variables
        while (counter < input.length() && input.charAt(counter) != '\n') {

            // Tokenize the concatenation operator
            if (input.charAt(counter) == '&') {
                tokens.add(new Token(Type.CONCATENATION, "&", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;

                // Tokenize newline character
            } else if (input.charAt(counter) == '$') {
                tokens.add(new Token(Type.NEXT_LINE, "$", new Position(position.getLine(),
                        position.getColumn())));
                position.add(1);
                counter++;

            } else if (input.charAt(counter) == '[') {

                tokens.add(
                        new Token(Type.ESCAPE_CODE_OPEN, "[", new Position(position.getLine(),
                                position.getColumn())));
                position.add(1);
                counter++;

                if (input.charAt(counter) == '[' && input.charAt(counter + 1) == ']') {
                    tokens.add(new Token(Type.SPECIAL_CHARACTER,
                            Character.toString(input.charAt(counter)),
                            new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter++;
                } else if (input.charAt(counter) == ']' && input.charAt(counter + 1) == ']') {
                    tokens.add(new Token(Type.SPECIAL_CHARACTER,
                            Character.toString(input.charAt(counter)),
                            new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter++;
                } else {
                    while (input.charAt(counter) != ']') {

                        // Skip trailing whitespace
                        while (counter < input.length() && input.charAt(counter) == ' ') {
                            position.add(1);
                            counter++;
                        }

                        tokens.add(new Token(Type.SPECIAL_CHARACTER,
                                Character.toString(input.charAt(counter)),
                                new Position(position.getLine(), position.getColumn())));
                        position.add(1);
                        counter++;
                    }
                }

                tokens.add(
                        new Token(Type.ESCAPE_CODE_CLOSE, "]", new Position(position.getLine(),
                                position.getColumn())));
                position.add(1);
                counter++;
            }

            // Tokenize quotation marks and string literal
            else if (input.charAt(counter) == '"') {
                tokens.add(new Token(Type.DELIMITER, Character.toString('"'),
                        new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;

                // Parse and tokenize the string literal
                StringBuilder stringLiteral = new StringBuilder();
                while (counter < input.length() && input.charAt(counter) != '\n' &&
                        input.charAt(counter) != '&') {
                    if (input.charAt(counter) == '"') {
                        tokens.add(new Token(Type.STRING_LITERAL, stringLiteral.toString(),
                                new Position(position.getLine(), position.getColumn())));
                        tokens.add(new Token(Type.DELIMITER, Character.toString('"'),
                                new Position(position.getLine(), position.getColumn())));
                        position.add(1);
                        counter++;
                        break;
                    }
                    stringLiteral.append(input.charAt(counter));
                    counter++;
                    position.add(1);
                }
            }
            // Tokenize Number Literals
            else if (Character.isDigit(input.charAt(counter))) {

                StringBuilder number = new StringBuilder();

                while (counter < input.length() &&
                        (Character.isDigit(input.charAt(counter)) || input.charAt(counter) == '.')) {
                    number.append(input.charAt(counter));
                    counter++;
                    position.add(1);
                }

                tokens.add(new Token(Type.LITERAL, number.toString(),
                        new Position(position.getLine(), position.getColumn())));
                continue;
            } else if (input.charAt(counter) == '+') {
                tokens.add(new Token(Type.ADD, "+", new Position(position.getLine(),
                        position.getColumn())));
                counter++;
                continue;
            } else if (input.charAt(counter) == '-') {

                Type last = tokens.get(tokens.size() - 1).getType();

                if (last == Type.ADD || last == Type.SUBTRACT || last == Type.MULTIPLY || last == Type.DIVIDE
                        || last == Type.MODULO || last == Type.GREATER || last == Type.LESS || last == Type.ASSIGNMENT
                        || last == Type.EQUAL || last == Type.GREATER_EQUAL || last == Type.LESS_EQUAL
                        || last == Type.NOT_EQUAL || last == Type.AND || last == Type.OR || last == Type.NOT
                        || last == Type.NEGATIVE || last == Type.POSITIVE || last == Type.LEFT_PARENTHESIS) {
                    tokens.add(new Token(Type.NEGATIVE, "-", new Position(position.getLine(),
                            position.getColumn())));
                    counter++;
                }

                tokens.add(new Token(Type.SUBTRACT, "-", new Position(position.getLine(),
                        position.getColumn())));
                counter++;
                continue;
            } else if (input.charAt(counter) == '*') {
                tokens.add(new Token(Type.MULTIPLY, "*", new Position(position.getLine(),
                        position.getColumn())));
                counter++;
                continue;
            } else if (input.charAt(counter) == '/') {
                tokens.add(new Token(Type.DIVIDE, "/", new Position(position.getLine(),
                        position.getColumn())));
                counter++;
                continue;
            } else if (input.charAt(counter) == '%') {
                tokens.add(new Token(Type.MODULO, "%", new Position(position.getLine(),
                        position.getColumn())));
                counter++;
                continue;
            } else if (input.charAt(counter) == '>') {
                if (input.charAt(counter + 1) == '=') {
                    tokens.add(new Token(Type.GREATER_EQUAL, ">=",
                            new Position(position.getLine(), position.getColumn())));
                    position.add(2);
                    counter += 2;
                } else {
                    tokens.add(new Token(Type.GREATER, ">", new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter++;
                }
            } else if (input.charAt(counter) == '<') {
                if (input.charAt(counter + 1) == '=') {
                    tokens.add(new Token(Type.LESS_EQUAL, "<=",
                            new Position(position.getLine(), position.getColumn())));
                    position.add(2);
                    counter += 2;
                } else if (input.charAt(counter + 1) == '>') {
                    tokens.add(new Token(Type.NOT_EQUAL, "<>",
                            new Position(position.getLine(), position.getColumn())));
                    position.add(2);
                    counter += 2;
                } else {
                    tokens.add(new Token(Type.LESS, "<", new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter++;

                }
            } else if (input.charAt(counter) == '=') {
                if (input.charAt(counter + 1) == '=') {
                    tokens.add(new Token(Type.EQUAL, "==",
                            new Position(position.getLine(), position.getColumn())));
                    position.add(2);
                    counter += 2;
                } else {
                    tokens.add(new Token(Type.ASSIGNMENT, "=", new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter++;
                }
            } else if (input.startsWith("AND", counter)) {
                tokens.add(new Token(Type.AND, "AND", new Position(position.getLine(), position.getColumn())));
                position.add("AND".length());
                counter += "AND".length();
            } else if (input.startsWith("OR", counter)) {
                tokens.add(new Token(Type.OR, "OR", new Position(position.getLine(), position.getColumn())));
                position.add("OR".length());
                counter += "OR".length();
            } else if (input.startsWith("NOT", counter)) {
                tokens.add(new Token(Type.NOT, "NOT", new Position(position.getLine(), position.getColumn())));
                position.add("NOT".length());
                counter += "NOT".length();
            } else if (input.charAt(counter) == '(') {
                tokens.add(
                        new Token(Type.LEFT_PARENTHESIS, "(", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else if (input.charAt(counter) == ')') {
                tokens.add(
                        new Token(Type.RIGHT_PARENTHESIS, ")", new Position(position.getLine(), position.getColumn())));
                position.add(1);
                counter++;
            } else {
                if (Character.isWhitespace(input.charAt(counter))) {
                    counter++;
                    position.add(1);
                    continue;
                }
                // Tokenize Identifiers
                // Parse the variable name
                StringBuilder variableName = new StringBuilder();
                while (counter < input.length() &&
                        !Character.isWhitespace(input.charAt(counter))
                        && input.charAt(counter) != '&' && input.charAt(counter) != '\n' &&
                        input.charAt(counter) != '+'
                        && input.charAt(counter) != '-' && input.charAt(counter) != '*' &&
                        input.charAt(counter) != '/'
                        && input.charAt(counter) != '%' && input.charAt(counter) != '<' &&
                        input.charAt(counter) != '>'
                        && input.charAt(counter) != '(' && input.charAt(counter) != ')'
                        && input.charAt(counter) != '=') {
                    variableName.append(input.charAt(counter));
                    counter++;
                    position.add(1);
                }
                tokens.add(new Token(Type.IDENTIFIER, variableName.toString(),
                        new Position(position.getLine(), position.getColumn())));
            }
        }

        return tokens;
    }

    private List<Token> checkIndentLevel(Position position) {

        int spaces = 0, tabs = 0, newIndentLevel;
        int temp = counter;
        List<Token> indentTokens = new ArrayList<>();

        while (temp < input.length() && (input.charAt(temp) == ' ' || input.charAt(temp) == '\t')) {
            if (input.charAt(temp) == ' ') {
                spaces++;
            } else {
                tabs++;
            }
            temp++;
        }

        if (spaces > 0 && tabs > 0) {
            System.err.println("Lexer Error: Mixing spaces and tabs for indentation: "
                    + new Position(position.getLine(), position.getColumn()));
            System.exit(1);
        } else if (spaces > 0) {
            if (spaces % 4 != 0) {
                System.err.println("Lexer Error: Invalid indentation found: "
                        + new Position(position.getLine(), position.getColumn()));
                System.exit(1);
            }

            newIndentLevel = spaces / 4;

            if (newIndentLevel > indentLevel) {
                for (int i = 0; i < newIndentLevel - indentLevel; i++) {
                    indentTokens
                            .add(new Token(Type.INDENT, "", new Position(position.getLine(), position.getColumn())));
                    position.add(4);
                    counter += 4;
                }
                indentLevel = newIndentLevel;

            } else if (newIndentLevel < indentLevel) {
                while (newIndentLevel < indentLevel) {
                    indentTokens
                            .add(new Token(Type.DEDENT, "", new Position(position.getLine(), position.getColumn())));
                    indentLevel--;
                }
            }
        } else if (tabs > 0) {

            System.out.println("Found a tab");

            newIndentLevel = tabs;

            if (newIndentLevel > indentLevel) {
                for (int i = 0; i < newIndentLevel - indentLevel; i++) {
                    indentTokens
                            .add(new Token(Type.INDENT, "", new Position(position.getLine(), position.getColumn())));
                    position.add(1);
                    counter += 1;
                }
                indentLevel = newIndentLevel;

            } else if (newIndentLevel < indentLevel) {

                while (newIndentLevel < indentLevel) {
                    indentTokens
                            .add(new Token(Type.DEDENT, "", new Position(position.getLine(), position.getColumn())));
                    indentLevel--;
                }
            }
        } else {
            newIndentLevel = 0;

            System.out.println("New Indent Level: " + newIndentLevel + " Indent Level: " + indentLevel);

            if (newIndentLevel < indentLevel) {
                while (newIndentLevel < indentLevel) {
                    indentTokens
                            .add(new Token(Type.DEDENT, "", new Position(position.getLine(), position.getColumn())));
                    indentLevel--;
                }
            }
        }
        return indentTokens;
    }
}