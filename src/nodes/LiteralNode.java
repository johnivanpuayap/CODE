package src.nodes;

import java.util.ArrayList;
import java.util.List;
import src.utils.Token;

public class LiteralNode extends ExpressionNode {

    private final Token value;
    private final List<Token> tokens = new ArrayList<>(1);

    public LiteralNode(Token value) {
        super(value.getPosition());
        this.value = value;
        tokens.add(value);
    }

    public Token getValue() {
        return value;
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