package src.nodes;

import src.utils.Position;

public class AssignmentNode extends StatementNode {

    private final ExpressionNode.Variable variable;
    private final ExpressionNode expressionNode;

    public AssignmentNode(ExpressionNode.Variable variable, ExpressionNode expressionNode, Position position) {
        super(position);
        this.variable = variable;
        this.expressionNode = expressionNode;
    }


    public ExpressionNode getExpression() {
        return expressionNode;
    }

    public ExpressionNode.Variable getVariable() {
        return variable;
    }
    
    @Override
    public String toString() {
        return "AssignmentStatementNode {" +
                "variable=" + variable +
                ", expressionNode=" + expressionNode +
                '}';
    }
}