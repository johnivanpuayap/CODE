package src.nodes;

import java.util.ArrayList;
import java.util.List;

import src.utils.Position;
import src.utils.Token;
import src.nodes.ExpressionNode;

public class AssignmentStatementNode extends StatementNode {

    private final VariableNode variable;
    private final ExpressionNode expressionNode;

    public AssignmentStatementNode(VariableNode variable, ExpressionNode expressionNode, Position position) {
        super(position);
        this.variable = variable;
        this.expressionNode = expressionNode;
    }
    
    @Override
    public String toString() {
        return "AssignmentStatementNode {" +
                "variable=" + variable +
                ", expressionNode=" + expressionNode +
                '}';
    }
}