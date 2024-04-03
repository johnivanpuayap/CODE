package src.nodes;

import java.util.ArrayList;
import java.util.List;

import src.utils.Position;
import src.utils.Token;
import src.nodes.ExpressionNode;

public class StatementNode extends ASTNode {
    private Token leftSide;
    private Token rightSide;
    private ExpressionNode expressionNode;
    private Position position;
    private String value;

    public StatementNode(Token leftSide, ExpressionNode expressionNode) {
        super(leftSide.getPosition());
        this.leftSide = leftSide;
        this.expressionNode = expressionNode;
        this.rightSide = null;
    }

    public StatementNode(Token leftSide, Token rightSide) {
        super(leftSide.getPosition());
        this.leftSide = leftSide;
        this.expressionNode = null;
        this.rightSide = rightSide;
    }

    // Getters for variable name, expression, and position

    public Token getLeftSide() {
        return leftSide;
    }

    public Token getRightSide() {
        return rightSide;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    public Position getPosition() {
        return position;
    }

    public String getValue() {
        return value;
    }

    // Method to check if the statement contains an expression
    public boolean hasExpression() {
        return expressionNode != null;
    }

    public List<Token> getTokens() {
        List<Token> tokens = new ArrayList<>();
        if (hasExpression()) {
            tokens.addAll(expressionNode.getTokens());
        } else {
            tokens.add(rightSide);
        }
        return tokens;
    }
}