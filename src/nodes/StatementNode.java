package src.nodes;
import src.utils.Position;

public class StatementNode extends ASTNode {
    private String variableName;
    private ExpressionNode expressionNode;
    private Position position;
    private String value;

    public StatementNode(String variableName, ExpressionNode expressionNode, Position position) {
        this.variableName = variableName;
        this.expressionNode = expressionNode;
        this.position = position;
    }

    public StatementNode(String variableName, String value, Position position) {
        this.variableName = variableName;
        this.expressionNode = null;
        this.value = value;
        this.position = position;
    }

    // Getters for variable name, expression, and position
    public String getVariableName() {
        return variableName;
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
}