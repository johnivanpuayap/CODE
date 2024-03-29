import java.util.List;
import java.util.ArrayList;

class Position {
    private int line;
    private int position;

    public Position(int line, int position) {
        this.line = line;
        this.position = position;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

}

// Lexer or Lexical Analyzer class to tokenize the input program
class Lexer {
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
                    if (currentChar == '\n') {
                        position.setLine(position.getLine() + 1);
                        position.setPosition(1);
                    } else {
                        position.setPosition(position.getPosition() + 1);
                    }

                    counter++;    
                    continue;
                }

                // Skip comments
                if (currentChar == '#') {
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
                        throw new RuntimeException("Newline required after BEGIN CODE at Line " + indentCheck.getLine() + ", Position " + indentCheck.getPosition());
                    }
                    
                    int[] result = moveToNextLine(counterForIndentation, indentCheck.getLine(), indentCheck.getPosition());
                    
                    counterForIndentation = result[0];
                    indentCheck.setLine(result[1]);
                    indentCheck.setPosition(result[2]);
                    
                    // Ensure proper indentation after "BEGIN CODE"
                    if (counterForIndentation < input.length() && Character.isWhitespace(input.charAt(counter))) {
                        currentIndent = findIndentLevel(counterForIndentation);

                        if (currentIndent == 0 && input.charAt(counterForIndentation) != '#'){
                            throw new RuntimeException("Indentation error after BEGIN CODE at Line " + indentCheck.getLine() + ", Position " + indentCheck.getPosition());
                        }
                    } else {
                        throw new RuntimeException("Indentation required after BEGIN CODE at Line "  + indentCheck.getLine() + ", Position " + indentCheck.getPosition());
                    }

                    // Check indentation for subsequent lines until "END CODE"
                    while (!input.startsWith("END CODE", counterForIndentation)) {
                        int indentLevel = findIndentLevel(counterForIndentation);

                        if (indentLevel != currentIndent && input.charAt(counterForIndentation) != '#') {
                            throw new RuntimeException("Improper indentation inside BEGIN CODE at Line "  + indentCheck.getLine() + ", Position " + indentCheck.getPosition());
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
                

                if (input.startsWith("INT", counter)) {
                    // Tokenize INT declaration
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
                        while (counter < input.length() && input.charAt(counter) != ',' && input.charAt(counter) != '=' && input.charAt(counter) != '\n') {
                            variableName.append(input.charAt(counter));
                            counter++;
                        }
                
                        // Add variable token
                        tokens.add(new Token(Token.Type.VARIABLE, variableName.toString(), position));
                        System.out.println("Variable name found at Line " + position.getLine() + ", Position " + position.getPosition());
                
                        // Check for optional initialization
                        if (counter < input.length() && input.charAt(counter) == '=') {
                            // Tokenize assignment operator
                            System.out.println("Assignment operator found at Line " + position.getLine() + ", Position " + position.getPosition());
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
                
                        // Skip trailing whitespace and comma
                        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
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


                // If none of the above conditions match, it's an invalid token
                if (input.charAt(counter) != '\n' && input.charAt(counter) != ' ' && input.charAt(counter) != '#') {
                    throw new RuntimeException("Invalid token " + input.charAt(counter) + " at Line " + position.getLine() + ", Position " + position.getPosition());
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
            throw new RuntimeException("Indentation with space should be 4 lines at Line " + position.getLine() + ", Position " + position.getPosition());
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
}