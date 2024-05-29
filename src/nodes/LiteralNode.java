package src.nodes;

import java.util.ArrayList;
import java.util.List;
import src.utils.Token;
import src.utils.Type;

public class LiteralNode extends ExpressionNode {

    private final Token value;
    private List<Token> tokens = new ArrayList<>(1);

    public LiteralNode(Token value) {
        super(value.getPosition());
        this.value = value;
        tokens.add(value);
    }

    public Token getValue() {
        return value;
    }

    public Type getDataType() {
        String lexeme = value.getLexeme();

        if (lexeme.matches("[0-9]+")) {
            return Type.INT;
        } else if (lexeme.matches("[0-9]+\\.[0-9]+")) {
            return Type.FLOAT;
        } else if (lexeme.matches("'[^']*'")) { // Updated regex for characters
            return Type.CHAR;
        } else if (lexeme.equals("\"TRUE\"") || lexeme.equals("\"FALSE\"")) {
            return Type.BOOL;
        } else if (lexeme.matches("\".*\"")) {
            return Type.STRING_LITERAL;
        }

        return null;
    }

    @Override
    public String toString() {
        return value.getLexeme();
    }

    @Override
    public List<Token> getTokens() {
        return tokens;
    }

    @Override
    public Token getToken(int index) {
        return tokens.get(index);
    }

    @Override
    public int countTokens() {
        return 1;
    }
}