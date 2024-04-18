package src.nodes;

public class AssignmentNode extends StatementNode {

    private final VariableNode variable;
    private final ExpressionNode expressionNode;

    public AssignmentNode(VariableNode variable, ExpressionNode expressionNode) {
        super(variable.getPosition());
        this.variable = variable;
        this.expressionNode = expressionNode;
    }


    public ExpressionNode getExpression() {
        return expressionNode;
    }

    public VariableNode getVariable() {
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