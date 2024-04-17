package src.utils;

public class Token {
    public enum Type {
        BEGIN_CODE, END_CODE, INDENT, DEDENT, NEWLINE, COMMA,
        INT, INT_LITERAL, CHAR, CHAR_LITERAL, FLOAT, FLOAT_LITERAL, BOOL, BOOL_LITERAL, 
        IDENTIFIER, ASSIGNMENT, ADD, SUBTRACT, MULTIPLY, DIVIDE, LEFT_PARENTHESIS, RIGHT_PARENTHESIS,
        POSITIVE, NEGATIVE,
        GREATER, LESS, GREATER_EQUAL, LESS_EQUAL, NOT_EQUAL, EQUAL,
        AND, OR, NOT,
        IF, ELSE_IF, ELSE, BEGIN_IF, END_IF,
        SCAN, SCAN_VALUE,
        DISPLAY, DELIMITER, CONCATENATION, STRING_LITERAL, COLON, NEXT_LINE, SPECIAL_CHARACTER, ESCAPE_CODE_OPEN, ESCAPE_CODE_CLOSE,
        WHILE, BEGIN_WHILE, END_WHILE
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
        if(
            type == Type.CHAR_LITERAL || 
            type == Type.INT_LITERAL || 
            type == Type.FLOAT_LITERAL || 
            type == Type.BOOL_LITERAL || 
            type == Type.STRING_LITERAL ||
            type == Type.SPECIAL_CHARACTER) {
            return "<" + type + ", " + "Initial Value: "  + initialValue + ">" + position;   
        } else if(type == Type.IDENTIFIER) {
            return "<" + type + ", " + "Name: "  + initialValue + "> " + position;
        } else {
            return "<" + type + ">" + position;
        }
    }
}