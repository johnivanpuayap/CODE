package src.utils;

public class Token {
    
    private Type type;
    private String lexeme;
    private Position position;

    public Token(Type type, String lexeme, Position position) {
        this.type = type;
        this.lexeme = lexeme;
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public Type getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    @Override
    public String toString() {
        if(
            type == Type.LITERAL || type == Type.SPECIAL_CHARACTER) {
            return "<" + type + ", " + "Value: "  + lexeme + ">" + position;   
        } else if(type == Type.IDENTIFIER) {
            return "<" + type + ", " + "Name: "  + lexeme + "> " + position;
        } else {
            return "<" + type + ">" + position;
        }
    }
}