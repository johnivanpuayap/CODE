package src.nodes;

import java.util.ArrayList;
import java.util.List;
import src.utils.Token;

public class OperatorNode extends ExpressionNode{

    private Token token;

    public OperatorNode(Token token) {
        this.token = token;
    }

    @Override
    public int countTokens() {
        return 1;
    }

    @Override
    public List<Token> getTokens() {
        List<Token> tokens = new ArrayList<>(1);
        tokens.add(token);
        return tokens;
    }

    @Override
    public Token getToken(int index) {
        if (index == 0) {
            return token;
        } else {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
    }
}
