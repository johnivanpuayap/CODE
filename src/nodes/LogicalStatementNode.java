package src.nodes;

import java.util.ArrayList;
import java.util.List;
import src.utils.Position;
import src.utils.Token;
import src.utils.Type;

public class LogicalStatementNode extends StatementNode {
    private final ExpressionNode leftExpression;
    private final Token operator;
    private final ExpressionNode rightExpression;
    private final List<Token> tokens;

    public LogicalStatementNode(ExpressionNode leftExpression, Token operator, ExpressionNode rightExpression) {
        super(leftExpression.getPosition());
        this.leftExpression = leftExpression;
        this.operator = operator;
        this.rightExpression = rightExpression;

        // Construct the tokens list
        tokens = new ArrayList<>();
        tokens.add(new Token(Type.LEFT_PARENTHESIS, "(", new Position(0, 0)));
        tokens.addAll(leftExpression.getTokens());
        tokens.add(operator);
        tokens.addAll(rightExpression.getTokens());
        tokens.add(new Token(Type.RIGHT_PARENTHESIS, ")", new Position(0, 0)));
    }

    public ExpressionNode getLeftExpression() {
        return leftExpression;
    }

    public Token getOperator() {
        return operator;
    }

    public ExpressionNode getRightExpression() {
        return rightExpression;
    }

    @Override
    public String toString() {
        return "(" + leftExpression.toString() + " " + operator.getLexeme() + " " + rightExpression.toString() + ")";
    }
}