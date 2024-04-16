package src.utils;

public class Token {
    public enum Type {
        BEGIN_CODE, END_CODE, 
        INT, INT_LITERAL, CHAR, CHAR_LITERAL, FLOAT, FLOAT_LITERAL, BOOL, BOOL_LITERAL, 
        IDENTIFIER, ASSIGNMENT, OPERATOR, 
        SCAN, SCAN_VALUE, 
        PARENTHESES, ESCAPE_CODE,
        DISPLAY_VARIABLE, DELIMITER, FUNCTION, CONCATENATION, STRING_LITERAL, COLON, SPECIAL_CHARACTER, VALUE,
        INDENT, DEDENT, NEWLINE, COMMA
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
        if(type == Type.CHAR_LITERAL || type == Type.INT_LITERAL || type == Type.FLOAT_LITERAL || type == Type.BOOL_LITERAL) {
            return "<" + type + ", " + "Initial Value: "  + initialValue +  " Current Value: "  + currentValue + ">";   
        }
        else if(type == Type.PARENTHESES || type == Type.OPERATOR || type == Type.STRING_LITERAL) {
            return "<" + type + ", " + "Value: "  + initialValue + ">";
        }
        else if(type == Type.IDENTIFIER) {
            return "<" + type + ", " + "Name: "  + initialValue + ">";
        }
        else {
            return "<" + type + ">";
        }
    }
}