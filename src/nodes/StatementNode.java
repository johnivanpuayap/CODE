package src.nodes;
import src.utils.Position;

public abstract class StatementNode extends ASTNode {

    public StatementNode(Position position) {
        super(position);
    }


    @Override
    public String toString() {
        return "StatementNode { }";
    }
}