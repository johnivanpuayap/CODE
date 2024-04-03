package src.nodes;
import src.utils.Position;

public class DeclarationNode extends ASTNode {
    private String dataType;
    private String variableName;
    private String value;
    private Position position;

    public DeclarationNode(String dataType, String variableName, String value, Position position) {
        super(position);
        this.dataType = dataType;
        this.variableName = variableName;
        this.value = value;
    }

    public DeclarationNode(String dataType, String variableName, Position position) {
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
}