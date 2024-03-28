class Token {
    public enum Type {
        BEGIN_CODE, END_CODE
    }

    private Type type;
    private String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
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