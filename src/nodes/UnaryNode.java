package src.nodes;

import java.util.List;
import java.util.ArrayList;
import src.utils.Token;

public class UnaryNode extends ExpressionNode {
    private final Token operator;
    private final ExpressionNode operand;
    private final List<Token> tokens = new ArrayList<>();

    public UnaryNode(Token operator, ExpressionNode operand) {
        super(operator.getPosition());
        this.operator = operator;
        this.operand = operand;

        tokens.add(operator);
        tokens.addAll(operand.getTokens());
    }

    public Token getOperator() {
        return operator;
    }

    public ExpressionNode getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        return operator.getLexeme() + operand.toString();
    }

    @Override
    public int countTokens() {
        return operand.countTokens() + 1; // Add 1 for the operator token
    }

    @Override
    public List<Token> getTokens() {
        return tokens;
    }

    @Override
    public Token getToken(int index) {
        return tokens.get(index);
    }
}