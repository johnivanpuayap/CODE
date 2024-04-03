package src.utils;

public class Variable {
    private String dataType;
    private String value;
    private Position position;

    public Variable(String dataType, String value, Position position) {
        this.dataType = dataType;
        this.value = value;
        this.position = position;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getDataType() {
        return dataType;
    }

    public Position position() {
        return position;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "dataType='" + dataType + '\'' +
                ", value='" + value + '\'' +
                ", position=" + position +
                '}';
    }
}