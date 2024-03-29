class DeclarationNode {
    private String dataType;
    private String variableName;
    private String value;
    private Position position;

    public DeclarationNode(String dataType, String variableName, String value, Position position) {
        this.dataType = dataType;
        this.variableName = variableName;
        this.value = value;
        this.position = position;
    }

    public DeclarationNode(String dataType, String variableName, Position position) {
        this.dataType = dataType;
        this.variableName = variableName;
        this.value = null;
        this.position = position;
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

    public Position getPosition() {
        return position;
    }
}