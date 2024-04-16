package src.nodes;
import src.utils.Position;

public class VariableNode extends ASTNode {
    private String variableName;

    public VariableNode(String variableName, Position position) {
        super(position);
        this.variableName = variableName;
    }

    // Getters for value and position
    public String getVariableName() {
        return variableName;
    }

    public Position getPosition() {
        return super.getPosition();
    }

    @Override
    public String toString() {
        return variableName;
    }
}