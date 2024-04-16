package src.nodes;
import src.utils.Position;

public class StringLiteralNode extends ASTNode {
    private String value;

    public StringLiteralNode(String value, Position position) {
        super(position);
        this.value = value;
    }

    // Getters for value and position
    public String getValue() {
        return value;
    }

    public Position getPosition() {
        return super.getPosition();
    }

    @Override
    public String toString() {
        return value;
    }
}