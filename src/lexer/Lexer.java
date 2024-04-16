package src.lexer;
import java.util.List;
import java.util.ArrayList;
import src.utils.Position;
import src.utils.Token;


public class Lexer {
    private final String input;
    private Position position;
    private int counter;
    private int indentLevel;

    public Lexer(String input) {
        this.input = input;
        this.position = new Position(1, 0);
        this.counter = 0;
        this.indentLevel = 0;
    }

    public List<Token> tokenize() {

        List<Token> tokens = new ArrayList<>();

        while (counter < input.length()) {
            char currentChar = input.charAt(counter);

            System.out.println("Current Char: " + currentChar);

            if(input.startsWith("BEGIN CODE", counter)) {
                tokens.add(new Token(Token.Type.BEGIN_CODE, "BEGIN CODE", position));
                position.add("BEGIN CODE".length());
                counter += "BEGIN CODE".length();
            } else if(input.startsWith("END CODE", counter)) {
                tokens.add(new Token(Token.Type.END_CODE, "END CODE", position));
                position.add("END CODE".length());
                counter += "END CODE".length();
            } else if(input.startsWith("DISPLAY", counter)) {
                tokens.add(new Token(Token.Type.FUNCTION, "DISPLAY", position));
                position.add("DISPLAY".length());
                counter += "DISPLAY".length();
                tokens = tokenizeDisplay(tokens);
            } else if(input.startsWith("SCAN", counter)) {
                tokens.add(new Token(Token.Type.SCAN, "SCAN", position));
                position.add("SCAN".length());
            } else if (Character.isLetter(currentChar)) {
                tokens.add(tokenizeIdentifier());
            } else if (Character.isDigit(currentChar)) {
                tokens.add(tokenizeNumber());
            } else if (currentChar == '=') {
                tokens.add(new Token(Token.Type.ASSIGNMENT, "=", position));
                position.add(1);
                counter++;
            } else if (currentChar == ':') {
                tokens.add(new Token(Token.Type.COLON, ":", position));
                position.add(1);
                counter++;
            } else if (currentChar == ',') {
                tokens.add(new Token(Token.Type.COMMA, ",", position));
                position.add(1);
                counter++;
            } else if(currentChar == '\'') {
                tokens.add(tokenizeCharLiteral());     
            } else if(currentChar == '\"') {
                tokens.add(tokenizeBooleanLiteral());
            } else if (currentChar == '&') {
                tokens.add(new Token(Token.Type.CONCATENATION, "$", position));
                position.add(1);
                counter++;
            } else if (currentChar == '$') {
                tokens.add(new Token(Token.Type.SPECIAL_CHARACTER, "$", position));
                position.add(1);
                counter++;
            }
            else if (Character.isWhitespace(currentChar)) {
                // Skip whitespace
                if (currentChar == '\n') {
                    tokens.add(new Token(Token.Type.NEWLINE, "\n", position));
                    position.add(1);
                    counter++;
                    
                    System.out.println("Checking indent level");
                    int newIndentLevel = countIndent();
                    if (newIndentLevel > indentLevel) {
                        tokens.add(new Token(Token.Type.INDENT, "", position));
                        position.add(newIndentLevel * 4);
                        counter += newIndentLevel * 4;
                        indentLevel = newIndentLevel;
                    } else if (newIndentLevel < indentLevel) {
                        while (newIndentLevel < indentLevel) {
                            tokens.add(new Token(Token.Type.DEDENT, "", position));
                            position.add(newIndentLevel * 4);
                            counter += newIndentLevel * 4;
                            indentLevel--;
                        }
                    }
                } else {
                    position.add(1);
                    counter++;
                }
            } else {
                // Invalid character
                throw new IllegalArgumentException("Invalid character: " + currentChar + position);
            }
        }



        return tokens;
    }

    private Token tokenizeIdentifier() {
        StringBuilder identifier = new StringBuilder();
        while (counter < input.length() && Character.isLetterOrDigit(input.charAt(counter))) {
            identifier.append(input.charAt(counter));
            position.add(1);
            counter++;
        }

        String identifierStr = identifier.toString();
        
        if (identifierStr.equals("INT")) {
            return new Token(Token.Type.INT, "INT", position);
        } else if (identifierStr.equals("CHAR")) {
            return new Token(Token.Type.CHAR, "CHAR", position);
        } else if (identifierStr.equals("FLOAT")) {
            return new Token(Token.Type.FLOAT, "FLOAT", position);
        } else if (identifierStr.equals("BOOL")) {
            return new Token(Token.Type.BOOL, "BOOL", position);
        } else {
            return new Token(Token.Type.IDENTIFIER, identifierStr, position);
        }
    }

    private Token tokenizeNumber() {
        StringBuilder number = new StringBuilder();
        boolean hasDecimal = false;
    
        while (counter < input.length()) {
            char currentChar = input.charAt(counter);
            if (Character.isDigit(currentChar)) {
                number.append(currentChar);
                position.add(1);
                counter++;
            } else if (currentChar == '.' && !hasDecimal) {
                number.append(currentChar);
                position.add(1);
                counter++;
                hasDecimal = true;
            } else {
                break;
            }
        }
    
        return new Token(hasDecimal ? Token.Type.FLOAT_LITERAL : Token.Type.INT_LITERAL, number.toString(), position);
    }

    private Token tokenizeCharLiteral() {
        StringBuilder charLiteral = new StringBuilder();
    
        // Store the opening single quote
        charLiteral.append(input.charAt(counter));
        position.add(1);
        counter++;
    
        while (counter < input.length() && input.charAt(counter) != '\n') {
            char currentChar = input.charAt(counter);
            if (currentChar == '\'') {
                // Store the closing single quote and create the CHAR_LITERAL token
                charLiteral.append(currentChar);
                position.add(1);
                counter++;
                return new Token(Token.Type.CHAR_LITERAL, charLiteral.toString(), position);
            } else {
                // Append regular characters
                charLiteral.append(currentChar);
                position.add(1);
                counter++;
            }
        }
    
        // If no closing single quote is found, store characters until the next single quote or newline
        while (counter < input.length() && input.charAt(counter) != '\'' && input.charAt(counter) != '\n') {
            charLiteral.append(input.charAt(counter));
            position.add(1);
            counter++;
        }
    
        // Return the CHAR_LITERAL token with the stored characters
        return new Token(Token.Type.CHAR_LITERAL, charLiteral.toString(), position);
    }

    private Token tokenizeBooleanLiteral() {
        StringBuilder boolLiteral = new StringBuilder();
    
        boolLiteral.append(input.charAt(counter));
        position.add(1);
        counter++;
    
        while (counter < input.length() && input.charAt(counter) != '\n') {
            char currentChar = input.charAt(counter);
            if (currentChar == '\"') {
                // Store the closing single quote and create the CHAR_LITERAL token
                boolLiteral.append(currentChar);
                position.add(1);
                counter++;
                return new Token(Token.Type.BOOL_LITERAL, boolLiteral.toString(), position);
            } else {
                // Append regular characters
                boolLiteral.append(currentChar);
                position.add(1);
                counter++;
            }
        }
    
        // If no closing single quote is found, store characters until the next " or newline
        while (counter < input.length() && input.charAt(counter) != '\'' && input.charAt(counter) != '\n') {
            boolLiteral.append(input.charAt(counter));
            position.add(1);
            counter++;
        }
    
        // Return the CHAR_LITERAL token with the stored characters
        return new Token(Token.Type.BOOL_LITERAL, boolLiteral.toString(), position);
    }

    private List<Token> tokenizeDisplay(List<Token> tokens) {
        
        // Parse the display string
        if (input.charAt(counter) == ':') {
            tokens.add(new Token(Token.Type.COLON, ":", position));
            position.setPosition(position.getPosition() + 1);
            counter++;
        }

        // Skip trailing whitespace
        while (counter < input.length() && input.charAt(counter) == ' ') {
            position.setPosition(position.getPosition() + 1);
            counter++;
        }

        // Parse the concatenated string and variables
        while (counter < input.length() && input.charAt(counter) != '\n') {

            // Tokenize the concatenation operator
            if (input.charAt(counter) == '&') {
                tokens.add(new Token(Token.Type.CONCATENATION, "&", position));
                position.setPosition(position.getPosition() + 1);
                counter++;

            // Tokenize newline character
            } else if (input.charAt(counter) == '$') {
                tokens.add(new Token(Token.Type.SPECIAL_CHARACTER, "$", position));
                position.setPosition(position.getPosition() + 1);
                counter++;
            } else if (input.charAt(counter) == '[') {
                tokens.add(new Token(Token.Type.SPECIAL_CHARACTER, "[", position));
                position.setPosition(position.getPosition() + 1);
                counter++;

                while (input.charAt(counter) != ']') {

                    // Skip trailing whitespace
                    while (counter < input.length() && input.charAt(counter) == ' ') {
                        position.setPosition(position.getPosition() + 1);
                        counter++;
                    }

                    tokens.add(new Token(Token.Type.VALUE, Character.toString(input.charAt(counter)), position));
                    position.setPosition(position.getPosition() + 1);
                    counter++;
                }

                tokens.add(new Token(Token.Type.SPECIAL_CHARACTER, "]", position));
                position.setPosition(position.getPosition() + 1);
                counter++;
            }
            // Tokenize quotation marks and string literal
            else if (input.charAt(counter) == '"') {
                tokens.add(new Token(Token.Type.DELIMITER, Character.toString('"'), position));
                position.setPosition(position.getPosition() + 1);
                counter++;

                // Parse and tokenize the string literal
                StringBuilder stringLiteral = new StringBuilder();
                while (counter < input.length() && input.charAt(counter) != '\n' && input.charAt(counter) != '&') {
                    if (input.charAt(counter) == '"') {
                        tokens.add(new Token(Token.Type.STRING_LITERAL, stringLiteral.toString(), position));
                        tokens.add(new Token(Token.Type.DELIMITER, Character.toString('"'), position));
                        position.setPosition(position.getPosition() + 1);
                        counter++;
                        break;
                    }
                    stringLiteral.append(input.charAt(counter));
                    counter++;
                    position.setPosition(position.getPosition() + 1);
                }
            } else {
                if (Character.isWhitespace(input.charAt(counter))) {
                    counter++;
                    position.setPosition(position.getPosition() + 1);
                    continue;
                }
                // Parse the variable name
                StringBuilder variableName = new StringBuilder();
                while (counter < input.length() && !Character.isWhitespace(input.charAt(counter)) && input.charAt(counter) != '&') {
                    variableName.append(input.charAt(counter));
                    counter++;
                    position.setPosition(position.getPosition() + 1);
                }
                tokens.add(new Token(Token.Type.DISPLAY_VARIABLE, variableName.toString(), position));
            }
        }

        return tokens;
    }

    private int countIndent() {
        int indent = 0;
        int temp = counter;

        while (temp < input.length() && (input.charAt(temp) == ' ' || input.charAt(temp) == '\t')) {
            if (input.charAt(temp) == ' ') {
                indent++;
            } else {
                indent += 4;
            }
            temp++;
        }
        return indent / 4; // Divide by 4 to count four spaces as one level of indentation
    }
}