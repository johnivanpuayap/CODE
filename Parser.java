import java.util.List;

// Syntax Analyzer or Parser class to generate Abstract Syntax Tree (AST) from tokens

class Parser {
    private List<Token> tokens;
    private int currentTokenIndex;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }

    public void parse() {
        if(!match(Token.Type.BEGIN_CODE)) {
            error("Expected BEGIN CODE");
        }

        if (!match(Token.Type.END_CODE)) {
            error("Expected END CODE");
        }
    }

    private boolean match(Token.Type type) {
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() == type) {
            currentTokenIndex++;
            return true;
        }
        return false;
    }

    private void error(String message) {
        throw new RuntimeException(message);
    }
}