package src.utils;

public class Token {
    public enum Type {
        BEGIN_CODE, END_CODE, DATA_TYPE, VARIABLE, ASSIGNMENT, VALUE
    }

    private Type type;
    private String value;
    private Position position;

    public Token(Type type, String value, Position position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "<" + type + ", " + value + ">";
    }
}