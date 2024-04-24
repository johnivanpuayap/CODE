package src.nodes;

import java.util.List;
import java.util.ArrayList;

import src.utils.Token;

public class VariableNode extends ExpressionNode {
    private final Token name;
    private List<Token> tokens = new ArrayList<>(1);

    public VariableNode(Token name) {
        super(name.getPosition());
        this.name = name;
        tokens.add(name);
    }

    public String getName() {
        return name.getLexeme();
    }

    @Override
    public String toString() {
        return name.getLexeme();
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
        return 1; // Variables will always have one token
    }
}