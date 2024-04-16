package src.nodes;
import src.utils.Position;

public class VariableDeclarationNode extends ASTNode {
    private String dataType;
    private String variableName;
    private String value;
    private Position position;

    public VariableDeclarationNode(String dataType, String variableName, String value, Position position) {
        super(position);
        this.dataType = dataType;
        this.variableName = variableName;
        this.value = value;
    }

    public VariableDeclarationNode(String dataType, String variableName, Position position) {
        super(position);
        this.dataType = dataType;
        this.variableName = variableName;
        this.value = null;
    }

    // Getters for data type, variable name, and value
    public String getDataType() {
        return dataType;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Position getPosition() {
        return super.getPosition();
    }

    @Override
    public String toString() {
        if (value != null) {
            return String.format("%s %s = %s", dataType, variableName, value);
        } else {
            return String.format("%s %s", dataType, variableName);
        }
    }
}