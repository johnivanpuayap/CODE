package src.nodes;

import src.utils.Position;

public class ContinueNode extends StatementNode {

    public ContinueNode(Position position) {
        super(position);
    }

    @Override
    public String toString() {
        return "ContinueNode";
    }
}
