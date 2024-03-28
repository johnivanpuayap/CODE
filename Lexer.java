import java.util.List;
import java.util.ArrayList;
// Lexer or Lexical Analyzer class to tokenize the input program

class Lexer {
    private String input;
    private int position;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
    }

    public List<Token> tokenize() {
       List<Token> tokens = new ArrayList<>();

        while(position < input.length()) {
            char currentChar = input.charAt(position);

            // Skip whitespaces
            if(Character.isWhitespace(currentChar)) {
                position++;
                continue;
            }

            // Skip comments
            if(currentChar == '#') {
                while (position < input.length() && input.charAt(position) != '\n') {
                    position++;
                }
                position++;
                continue;
            }

            if(input.startsWith("BEGIN CODE", position)) {
                tokens.add(new Token(Token.Type.BEGIN_CODE, "BEGIN CODE"));
                position += "BEGIN CODE".length();
            }

            if(input.startsWith("END CODE", position)) {
                tokens.add(new Token(Token.Type.END_CODE, "END CODE"));
                position += "END CODE".length();
            }
        }

        return tokens;
    }

    // Helper methods for lexer
}