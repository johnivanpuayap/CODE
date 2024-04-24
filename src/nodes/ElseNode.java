package src.nodes;

import java.util.List;

import src.utils.Position;

public class ElseNode extends StatementNode {
    private final List<StatementNode> statements;

    public ElseNode(List<StatementNode> statements, Position position) {
        super(position);
        this.statements = statements;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Else Statement {\n");
        sb.append("  statements = [\n");
        for (StatementNode statement : statements) {
            sb.append("    ").append(statement).append(",\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }
}
