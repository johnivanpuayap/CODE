package src.nodes;

import src.utils.Position;
import java.util.List;

public class WhileNode extends StatementNode {

    private final ExpressionNode condition;
    private final List<StatementNode> block;

    public WhileNode(ExpressionNode condition, List<StatementNode> block, Position position) {
        super(position);
        this.condition = condition;
        this.block = block;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public List<StatementNode> getStatements() {
        return block;
    }
}
