package src.utils;

public class Variable {
    private Type dataType;
    private String value;
    private Position position;

    public Variable(Type dataType, String value, Position position) {
        this.dataType = dataType;
        this.value = value;
        this.position = position;
    }

    public Variable(String value, Position position) {
        this.value = value;
        this.position = position;
        this.dataType = null;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Type getDataType() {
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