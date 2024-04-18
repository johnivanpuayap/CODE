package src.nodes;
import src.utils.Token;
import src.utils.Type;

public class VariableDeclarationNode extends ASTNode {
    private Token dataType;
    private Token identifier;
    private Token literal;
    private String value;

    public VariableDeclarationNode(Token dataType, Token identifier) {
        super(dataType.getPosition());
        this.dataType = dataType;
        this.identifier = identifier;
    }

    public VariableDeclarationNode(Token dataType, Token identifier, Token literal) {
        this(dataType, identifier);
        this.literal = literal;
        this.value = literal.getLexeme();
    }

    // Getters for data type, variable name, and value
    public Type getType() {
        return dataType.getType();
    }

    public String getName() {
        return identifier.getLexeme();
    }

    public String getValue() {
        if (literal != null) {
            return literal.getLexeme();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        if (value != null) {
            return String.format("%s %s = %s", dataType, identifier.getLexeme(), literal.getLexeme());
        } else {
            return String.format("%s %s", dataType, identifier.getLexeme());
        }
    }
}