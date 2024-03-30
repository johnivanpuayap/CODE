package src.nodes;

import src.utils.Position;

public class BreakNode extends StatementNode {

    public BreakNode(Position position) {
        super(position);
    }

    @Override
    public String toString() {
        return "BreakNode";
    }
}
