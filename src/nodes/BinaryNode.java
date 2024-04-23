package src.nodes;

import java.util.ArrayList;
import java.util.List;

import src.utils.Position;
import src.utils.Token;
import src.utils.Type;

public class BinaryNode extends ExpressionNode {
    private final Token operator;
    private final ExpressionNode left;
    private final ExpressionNode right;
    private List<Token> tokens = new ArrayList<>();

    public BinaryNode(ExpressionNode left, Token operator, ExpressionNode right) {
        super(left.getPosition());
        this.operator = operator;
        this.left = left;
        this.right = right;

        System.out.println("Left tokens: " + left.getTokens()); // Debug print
        System.out.println("Right tokens: " + right.getTokens()); // Debug print

        tokens.add(new Token(Type.RIGHT_PARENTHESIS, "(", new Position(0, 0)));
        tokens.addAll(left.getTokens());
        tokens.add(operator);
        tokens.addAll(right.getTokens());
        tokens.add(new Token(Type.LEFT_PARENTHESIS, ")", new Position(0, 0)));
    }

    public Token getOperator() {
        return operator;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "(" + left.toString() + operator.getLexeme() + right.toString() + ")";
    }

    @Override
    public int countTokens() {
        return left.countTokens() + right.countTokens() + 1; // Add 1 for the operator token
    }

    @Override
    public Token getToken(int index) {
        List<Token> tokens = getTokens();
        return tokens.get(index);
    }

    @Override
    public List<Token> getTokens() {
        return tokens;
    }
}