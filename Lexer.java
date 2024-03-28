import java.util.List;
import java.util.ArrayList;

// Lexer or Lexical Analyzer class to tokenize the input program
class Lexer {
    private String input;
    private int position;
    private int line;
    private int counter;

    public Lexer(String input) {
        this.input = input;
        this.counter = 0;
        this.position = 1;
        this.line = 1;
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
                    continue;
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
}