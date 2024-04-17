package src.nodes;
import src.utils.Position;
import src.utils.Token;

public class VariableNode extends ASTNode {
    private String variableName;
    private Token token;

    public VariableNode(Token token, Position position) {
        super(position);
        this.variableName = token.getValue();
        this.token = token;
    }

    // Getters for value and position
    public String getVariableName() {
        return variableName;
    }

    public Position getPosition() {
        return super.getPosition();
    }

    public Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        return variableName;
    }
}