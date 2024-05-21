package src.nodes;

import java.util.List;
import src.utils.Position;

public class ElseIfNode extends StatementNode {
    private final ExpressionNode condition;
    private final List<StatementNode> statements;

    public ElseIfNode(ExpressionNode condition, List<StatementNode> statements, Position position) {
        super(position);
        this.condition = condition;
        this.statements = statements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Else If Statement {\n");
        sb.append("  condition = ").append(condition).append(",\n");
        sb.append("  statements = [\n");
        for (StatementNode statement : statements) {
            sb.append("    ").append(statement).append(",\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }
}
