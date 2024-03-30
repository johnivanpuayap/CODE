package src.nodes;

import src.utils.Position;
import java.util.List;

public class ForNode extends StatementNode {

    private final AssignmentNode initialization;
    private final ExpressionNode condition;
    private final AssignmentNode update;
    private final List<StatementNode> block;

    public ForNode(AssignmentNode initialization, ExpressionNode condition, AssignmentNode update,
            List<StatementNode> block, Position position) {
        super(position);
        this.initialization = initialization;
        this.condition = condition;
        this.update = update;
        this.block = block;
    }

    public AssignmentNode getInitialization() {
        return initialization;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public AssignmentNode getUpdate() {
        return update;
    }

    public List<StatementNode> getStatements() {
        return block;
    }
}
