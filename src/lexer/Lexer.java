package src.lexer;
import java.util.List;
import java.util.ArrayList;
import src.utils.Token;
import src.utils.Position;

// Lexer or Lexical Analyzer class to tokenize the input program
public class Lexer {
    private String input;
    private int counter;
    private int currentIndent;

    private Position position;

    public Lexer(String input) {
        this.input = input;
        this.counter = 0;
        this.position = new Position(1, 1);
        this.currentIndent = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        try {
            while (counter < input.length()) {
                
                char currentChar = input.charAt(counter);

                // Skip whitespaces
                if (Character.isWhitespace(currentChar)) {
                    // System.out.println("BEFORE -> line: "+ position.getLine() + ", position: " + position.getPosition());
                    if (currentChar == '\n') {
                        position.setLine(position.getLine() + 1);
                        position.setPosition(1);
                    } else {
                        position.setPosition(position.getPosition() + 1);
                    }
                    // System.out.println("AFTER -> line: "+ position.getLine() + ", position: " + position.getPosition());
                    counter++;
                    continue;
                }

                // Skip comments
                if (currentChar == '#') {
                    // System.out.println("FOUND #COMMENT at -> line: "+ position.getLine() + ", position: " + position.getPosition());
                    while (counter < input.length() && input.charAt(counter) != '\n') {
                        counter++;
                        position.setPosition(position.getPosition() + 1);
                    }

                    if (counter < input.length() && input.charAt(counter) == '\n') {
                        position.setLine(position.getLine() + 1);
                        position.setPosition(1);
                        counter++;
                    }

                    continue;
                }

                if (input.startsWith("BEGIN CODE", counter)) {
                    tokens.add(new Token(Token.Type.BEGIN_CODE, "BEGIN CODE", position));
                    position.setPosition(position.getPosition() + "BEGIN CODE".length());
                    counter += "BEGIN CODE".length();
                    
                    // Will be used to check for indentation inside BEGIN CODE and END CODE block
                    int counterForIndentation = counter;
                    Position indentCheck = new Position(position.getLine(), position.getPosition());

                    // Check if there is a newline after "BEGIN CODE"
                    if (input.charAt(counterForIndentation) != '\n') {
                        error("Newline required after BEGIN CODE at Line " + indentCheck.getLine() + ", Position " + indentCheck.getPosition());
                    }
                    
                    int[] result = moveToNextLine(counterForIndentation, indentCheck.getLine(), indentCheck.getPosition());
                    
                    counterForIndentation = result[0];
                    indentCheck.setLine(result[1]);
                    indentCheck.setPosition(result[2]);
                    
                    // Ensure proper indentation after "BEGIN CODE"
                    if (counterForIndentation < input.length() && Character.isWhitespace(input.charAt(counter))) {
                        currentIndent = findIndentLevel(counterForIndentation);

                        if (currentIndent == 0 && input.charAt(counterForIndentation) != '#'){
                            error("Indentation error after BEGIN CODE at Line " + indentCheck.getLine() + ", Position " + indentCheck.getPosition());
                        }
                    } else {
                        error("Indentation required after BEGIN CODE at Line "  + indentCheck.getLine() + ", Position " + indentCheck.getPosition());
                    }

                    // Check indentation for subsequent lines until "END CODE"
                    while (!input.startsWith("END CODE", counterForIndentation)) {
                        int indentLevel = findIndentLevel(counterForIndentation);

                        if (indentLevel != currentIndent && input.charAt(counterForIndentation) != '#') {
                            error("Improper indentation inside BEGIN CODE at Line "  + indentCheck.getLine() + ", Position " + indentCheck.getPosition());
                        }
                        
                        result = moveToNextLine(counterForIndentation, indentCheck.getLine(), indentCheck.getPosition());

                        counterForIndentation = result[0];
                        indentCheck.setLine(result[1]);
                        indentCheck.setPosition(result[2]);
                    }

                }

                if (input.startsWith("END CODE", counter)) {
                    tokens.add(new Token(Token.Type.END_CODE, "END CODE", position));
                    position.setPosition(position.getPosition() + "END CODE".length());
                    counter += "END CODE".length();
                    continue;
                }
                
                // Tokenize INT declaration
                if (input.startsWith("INT", counter)) {
                    tokens.add(new Token(Token.Type.DATA_TYPE, "INT", position));
                    position.setPosition(position.getPosition() + "INT".length());
                    counter += "INT".length();
                
                    // Parse variable names and values
                    while (counter < input.length() && input.charAt(counter) != '\n') {
                
                        // Skip whitespace
                        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }
                
                        // Parse variable name
                        StringBuilder variableName = new StringBuilder();
                        Position variablePosition =  new Position(position.getLine(), position.getPosition());
                        while (counter < input.length() && input.charAt(counter) != ',' && input.charAt(counter) != '=' && input.charAt(counter) != '\n') {
                            variableName.append(input.charAt(counter));
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }
                
                        // Add variable token
                        tokens.add(new Token(Token.Type.VARIABLE, variableName.toString(), variablePosition));
                        // System.out.println("Variable name found at Line " + variablePosition.getLine() + ", Position " + variablePosition.getPosition());
                
                        // Check for optional initialization
                        if (counter < input.length() && input.charAt(counter) == '=') {
                            // Tokenize assignment operator
                            // System.out.println("Assignment operator found at Line " + position.getLine() + ", Position " + position.getPosition());
                            tokens.add(new Token(Token.Type.ASSIGNMENT, "=", position));
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                
                            // Parse value
                            StringBuilder value = new StringBuilder();
                            while (counter < input.length() && !Character.isWhitespace(input.charAt(counter)) && input.charAt(counter) != ',') {
                                value.append(input.charAt(counter));
                                position.setPosition(position.getPosition() + 1);
                                counter++;
                            }
                            tokens.add(new Token(Token.Type.VALUE, value.toString(), position));
                        }

                        // Break loop after reading newline
                        if (input.charAt(counter) == '\n') {
                            break;
                        }

                        // Skip trailing whitespace and comma
                        while (counter < input.length() && input.charAt(counter) == ' ') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }
                
                        if (counter < input.length() && input.charAt(counter) == ',') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                            
                            // Since there is a comma, we expect another variable name so we create a new data type token
                            tokens.add(new Token(Token.Type.DATA_TYPE, "INT", position));
                        } else {
                            break;
                        }
                    }
                    continue;
                }

                // Tokenize FLOAT declaration
                if (input.startsWith("FLOAT", counter)) {
                    tokens.add(new Token(Token.Type.DATA_TYPE, "FLOAT", position));
                    position.setPosition(position.getPosition() + "FLOAT".length());
                    counter += "FLOAT".length();
                
                    // Parse variable names and values
                    while (counter < input.length() && input.charAt(counter) != '\n') {
                
                        // Skip whitespace
                        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }
                
                        // Parse variable name
                        StringBuilder variableName = new StringBuilder();
                        Position variablePosition =  new Position(position.getLine(), position.getPosition());
                        while (counter < input.length() && input.charAt(counter) != ',' && input.charAt(counter) != '=' && input.charAt(counter) != '\n') {
                            variableName.append(input.charAt(counter));
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }
                        // Add variable token
                        tokens.add(new Token(Token.Type.VARIABLE, variableName.toString(), variablePosition));
                        // System.out.println("Variable name found at Line " + variablePosition.getLine() + ", Position " + variablePosition.getPosition());
                
                        // Check for optional initialization
                        if (counter < input.length() && input.charAt(counter) == '=') {
                            // Tokenize assignment operator
                            // System.out.println("Assignment operator found at Line " + position.getLine() + ", Position " + position.getPosition());
                            tokens.add(new Token(Token.Type.ASSIGNMENT, "=", position));
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                
                            // Parse value
                            StringBuilder value = new StringBuilder();
                            while (counter < input.length() && !Character.isWhitespace(input.charAt(counter)) && input.charAt(counter) != ',') {
                                value.append(input.charAt(counter));
                                position.setPosition(position.getPosition() + 1);
                                counter++;
                            }
                            tokens.add(new Token(Token.Type.VALUE, value.toString(), position));
                        }
                        
                        // Break loop after reading newline
                        if (input.charAt(counter) == '\n') {
                            break;
                        }

                        // Skip trailing whitespace and comma
                        while (counter < input.length() && input.charAt(counter) == ' ') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }
                
                        if (counter < input.length() && input.charAt(counter) == ',') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                            
                            // Since there is a comma, we expect another variable name so we create a new data type token
                            tokens.add(new Token(Token.Type.DATA_TYPE, "FLOAT", position));
                        } else {
                            break;
                        }
                    }
                    continue;
                }

                // Tokenize CHAR declaration
                if (input.startsWith("CHAR", counter)) {
                    tokens.add(new Token(Token.Type.DATA_TYPE, "CHAR", position));
                    position.setPosition(position.getPosition() + "CHAR".length());
                    counter += "CHAR".length();
                
                    // Parse variable names and values
                    while (counter < input.length() && input.charAt(counter) != '\n') {
                
                        // Skip whitespace
                        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }
                
                        // Parse variable name
                        StringBuilder variableName = new StringBuilder();
                        Position variablePosition =  new Position(position.getLine(), position.getPosition());
                        while (counter < input.length() && input.charAt(counter) != ',' && input.charAt(counter) != '=' && input.charAt(counter) != '\n') {
                            variableName.append(input.charAt(counter));
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }
                
                        // Add variable token
                        tokens.add(new Token(Token.Type.VARIABLE, variableName.toString(), variablePosition));
                        // System.out.println("Variable name found at Line " + variablePosition.getLine() + ", Position " + variablePosition.getPosition());
                
                        // Check for optional initialization
                        if (counter < input.length() && input.charAt(counter) == '=') {
                            // Tokenize assignment operator
                            // System.out.println("Assignment operator found at Line " + position.getLine() + ", Position " + position.getPosition());
                            tokens.add(new Token(Token.Type.ASSIGNMENT, "=", position));
                            position.setPosition(position.getPosition() + 1);
                            counter++;

                            // Parse value
                            StringBuilder value = new StringBuilder();
                            boolean start = true;
                            while (counter < input.length() && !Character.isWhitespace(input.charAt(counter)) && input.charAt(counter) != ',') {
                                
                                // Tokenize quotation marks and value
                                if (input.charAt(counter) == '\'') {
                                    if (start) {
                                        tokens.add(new Token(Token.Type.DELIMITER, Character.toString('\''), position));
                                        start = false;
                                    } else {
                                        tokens.add(new Token(Token.Type.VALUE, value.toString(), position));
                                        tokens.add(new Token(Token.Type.DELIMITER, Character.toString('\''), position));
                                        position.setPosition(position.getPosition() + 1);
                                        counter++;
                                        break;
                                    }
                                } else {
                                    value.append(input.charAt(counter));
                                }
                                position.setPosition(position.getPosition() + 1);
                                counter++;
                            }
                            if (!start) {
                                tokens.add(new Token(Token.Type.VALUE, value.toString(), position));
                            }
                        }
                        
                        // Break loop after reading newline
                        if (input.charAt(counter) == '\n') {
                            break;
                        }

                        // Skip trailing whitespace and comma
                        while (counter < input.length() && input.charAt(counter) == ' ') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }
                
                        if (counter < input.length() && input.charAt(counter) == ',') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                            
                            // Since there is a comma, we expect another variable name so we create a new data type token
                            tokens.add(new Token(Token.Type.DATA_TYPE, "CHAR", position));
                        } else {
                            break;
                        }
                    }
                    continue;
                }

                // Tokenize BOOL declaration
                if (input.startsWith("BOOL", counter)) {
                    tokens.add(new Token(Token.Type.DATA_TYPE, "BOOL", position));
                    position.setPosition(position.getPosition() + "BOOL".length());
                    counter += "BOOL".length();
                
                    // Parse variable names and values
                    while (counter < input.length() && input.charAt(counter) != '\n') {
                
                        // Skip whitespace
                        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }
                
                        // Parse variable name
                        StringBuilder variableName = new StringBuilder();
                        Position variablePosition =  new Position(position.getLine(), position.getPosition());
                        while (counter < input.length() && input.charAt(counter) != ',' && input.charAt(counter) != '=' && input.charAt(counter) != '\n') {
                            variableName.append(input.charAt(counter));
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }
                
                        // Add variable token
                        tokens.add(new Token(Token.Type.VARIABLE, variableName.toString(), variablePosition));
                        // System.out.println("Variable name found at Line " + variablePosition.getLine() + ", Position " + variablePosition.getPosition());
                
                        // Check for optional initialization
                        if (counter < input.length() && input.charAt(counter) == '=') {
                            // Tokenize assignment operator
                            // System.out.println("Assignment operator found at Line " + position.getLine() + ", Position " + position.getPosition());
                            tokens.add(new Token(Token.Type.ASSIGNMENT, "=", position));
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                
                            // Parse value
                            StringBuilder value = new StringBuilder();
                            boolean start = true;
                            while (counter < input.length() && !Character.isWhitespace(input.charAt(counter)) && input.charAt(counter) != ',') {
                                // Tokenize quotation marks and value
                                if (input.charAt(counter) == '"') {
                                    if (start) {
                                        tokens.add(new Token(Token.Type.DELIMITER, Character.toString('"'), position));
                                        start = false;
                                    } else {
                                        tokens.add(new Token(Token.Type.VALUE, value.toString(), position));
                                        tokens.add(new Token(Token.Type.DELIMITER, Character.toString('"'), position));
                                        position.setPosition(position.getPosition() + 1);
                                        counter++;
                                        break;
                                    }
                                } else {
                                    value.append(input.charAt(counter));
                                }
                                position.setPosition(position.getPosition() + 1);
                                counter++;
                            }
                            if (!start) {
                                tokens.add(new Token(Token.Type.VALUE, value.toString(), position));
                            }
                        }
                        
                        // Break loop after reading newline
                        if (input.charAt(counter) == '\n') {
                            break;
                        }

                        // Skip trailing whitespace and comma
                        while (counter < input.length() && input.charAt(counter) == ' ') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }
                
                        if (counter < input.length() && input.charAt(counter) == ',') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                            
                            // Since there is a comma, we expect another variable name so we create a new data type token
                            tokens.add(new Token(Token.Type.DATA_TYPE, "BOOL", position));
                        } else {
                            break;
                        }
                    }
                    continue;
                }

                // Tokenize DISPLAY declaration with concatenation
                if (input.startsWith("DISPLAY", counter)) {
                    tokens.add(new Token(Token.Type.FUNCTION, "DISPLAY", position));
                    position.setPosition(position.getPosition() + "DISPLAY".length());
                    counter += "DISPLAY".length();

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
                    continue;
                }


                // If none of the above conditions match, it's an invalid token
                if (input.charAt(counter) != '\n' && input.charAt(counter) != ' ' && input.charAt(counter) != '#') {
                    error("Invalid token " + input.charAt(counter) + " at Line " + position.getLine() + ", Position " + position.getPosition());
                }
                
            }
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return tokens;
    }

    // Helper methods for lexer

    private int findIndentLevel(int startIndex) {
        
        int i = startIndex;
        int indentLevel = 0;
        int spaceCount = 0;

        // Check for tabs
        while (i < input.length() && input.charAt(i) == '\t') {
            indentLevel++;
            i++;
        }

        if (indentLevel > 0) {
            return indentLevel;
        }

        // Check for spaces
        while (i < input.length() && input.charAt(i) == ' ') {
            spaceCount++;
            i++;
        }

        if (spaceCount % 4 == 0) {
            return spaceCount / 4;
        } else if (spaceCount > 0 && spaceCount % 4 != 0) {
            error("Indentation with space should be 4 lines at Line " + position.getLine() + ", Position " + position.getPosition());
        }

        return 0;
    }

    private int[] moveToNextLine(int startIndex, int line, int position) {
        int i = startIndex;
        
        while (i < input.length() && input.charAt(i) != '\n') {
            i++;
        }
        
        if (i < input.length() && input.charAt(i) == '\n') {
            line++;
            position = 1;
        }

        return new int[]{(i + 1), line, position};
    }

    private void error(String message) {
        throw new RuntimeException("Lexer error: " + message);
    }
}