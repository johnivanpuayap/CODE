package src.utils;

public class Token {
    public enum Type {
        BEGIN_CODE, END_CODE, DATA_TYPE, VARIABLE, ASSIGNMENT, VALUE, OPERATOR, SCAN, ESCAPE_CODE, SCAN_VALUE, PARENTHESES,
        DISPLAY_VARIABLE, DELIMITER, FUNCTION, CONCATENATION, STRING_LITERAL, COLON, SPECIAL_CHARACTER
    }

    private Type type;
    private String initialValue;
    private String currentValue;
    private Position position;

    public Token(Type type, String value, Position position) {
        this.type = type;
        this.initialValue = value;
        this.currentValue = value;
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return initialValue;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    @Override
    public String toString() {
        if(type == Type.VALUE){
            return "<" + type + ", " + "Initial Value: "  + initialValue +  " Current Value: "  + currentValue + ">";   
        }
        else if(type == Type.DATA_TYPE || type == Type.PARENTHESES || type == Type.OPERATOR || type == Type.STRING_LITERAL) {
            return "<" + type + ", " + "Value: "  + initialValue + ">";
        }
        else if(type == Type.VARIABLE) {
            return "<" + type + ", " + "Name: "  + initialValue + ">";
        }
        else {
            return "<" + type + ">";
        }
    }
}