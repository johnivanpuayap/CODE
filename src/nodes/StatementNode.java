package src.nodes;

import java.util.ArrayList;
import java.util.List;

import src.utils.Position;
import src.utils.Token;
import src.nodes.ExpressionNode;

public abstract class StatementNode extends ASTNode {
    private Position position;

    public StatementNode(Position position) {
        super(position);
    }


    @Override
    public String toString() {
        return "StatementNode{}";
    }
}