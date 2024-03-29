import java.util.List;
import java.util.ArrayList;

// Lexer or Lexical Analyzer class to tokenize the input program
class Lexer {
    private String input;
    private int position;
    private int line;
    private int counter;
    private int currentIndent;

    public Lexer(String input) {
        this.input = input;
        this.counter = 0;
        this.position = 1;
        this.line = 1;
        this.currentIndent = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        try {
            while (counter < input.length()) {
                
                char currentChar = input.charAt(counter);
                
                System.out.println("Current Char: " + currentChar + " at LINE: " + line + " at POSITION: " + position);

                // Skip whitespaces
                if (Character.isWhitespace(currentChar)) {
                    if (currentChar == '\n') {
                        line++;
                        position = 1;
                    } else {
                        position++;
                    }

                    counter++;    
                    continue;
                }

                // Skip comments
                if (currentChar == '#') {
                    while (counter < input.length() && input.charAt(counter) != '\n') {
                        counter++;
                    }

                    if (counter < input.length() && input.charAt(counter) == '\n') {
                        line++;
                        position = 1;
                    }

                    counter++;
                    continue;
                }

                if (input.startsWith("BEGIN CODE", counter)) {
                    System.out.println("Token found: " + Token.Type.BEGIN_CODE + " at position: " + position + " at line: " + line);
                    tokens.add(new Token(Token.Type.BEGIN_CODE, "BEGIN CODE", line));
                    position += "BEGIN CODE".length();
                    counter += "BEGIN CODE".length();
                    
                    int counterForIndentation = counter;

                    // Check if there is a newline after "BEGIN CODE"
                    if (input.charAt(counterForIndentation) != '\n') {
                        throw new RuntimeException("Newline required after BEGIN CODE at Line " + line + ", Position " + position);
                    }
                    counterForIndentation = moveToNextLine(counterForIndentation);

                    // Ensure proper indentation after "BEGIN CODE"
                    if (counterForIndentation < input.length() && Character.isWhitespace(input.charAt(counter))) {
                        currentIndent = findIndentLevel(counterForIndentation);
                        if (currentIndent == 0) {
                            throw new RuntimeException("Indentation error after BEGIN CODE at Line " + line + ", Position " + position);
                        }
                    } else {
                        throw new RuntimeException("Indentation required after BEGIN CODE at Line " + line + ", Position " + position);
                    }

                    // Check indentation for subsequent lines until "END CODE"
                    while (!input.startsWith("END CODE", counterForIndentation)) {
                        int indentLevel = findIndentLevel(counterForIndentation);
                        if (indentLevel != currentIndent) {
                            throw new RuntimeException("Improper indentation inside BEGIN CODE at Line " + line + ", Position " + position);
                        }
                        counterForIndentation = moveToNextLine(counterForIndentation); // Move to the next line
                    }

                }

                if (input.startsWith("END CODE", counter)) {
                    tokens.add(new Token(Token.Type.END_CODE, "END CODE", line));
                    position += "END CODE".length();
                    counter += "END CODE".length();
                    continue;
                }

                // Tokenize reserved words using regular expressions with word boundaries
                for (Token.Type type : Token.Type.values()) {

                    // Skip BEGIN_CODE and END_CODE tokens
                    if (type == Token.Type.BEGIN_CODE || type == Token.Type.END_CODE) {
                        continue;
                    }

                    int tokenIndex = input.indexOf(type.toString(), counter); // Find the index of the token
                    
                    if (tokenIndex == counter) {
                        // Token found starting at the current position
                        System.out.println("Token found: " + type + " at LINE: " + line + " at POSITION: " + position);
                        tokens.add(new Token(type, type.toString(), line));
                        position += type.toString().length();
                        counter += type.toString().length();

                        System.out.print(input.charAt(counter));
                        break;
                    }
                }
                
                // If none of the above conditions match, it's an invalid token
                if (input.charAt(counter) != '\n' && input.charAt(counter) != ' ' && input.charAt(counter) != '#') {
                    throw new RuntimeException("Invalid token at Line " + line + ", Position " + position);
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
            throw new RuntimeException("Indentation with space should be 4 lines at Line " + line + ", Position " + position);
        }

        return 0;
    }

    private int moveToNextLine(int startIndex) {
        int i = startIndex;
        
        while (i < input.length() && input.charAt(i) != '\n') {
            i++;
        }
        
        if (i < input.length() && input.charAt(i) == '\n') {
            line++;
            position = 1;
        }
        return i + 1; // Move to the next character after newline
    }
}