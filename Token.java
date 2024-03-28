// Token class to represent individual tokens from the input

class Token {
    public enum Type {
        BEGIN_CODE, END_CODE, INT
    }

    private Type type;
    private String value;
    private int line;

    public Token(Type type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "<" + type + ", " + value + ">";
    }
}