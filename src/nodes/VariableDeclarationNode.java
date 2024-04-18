package src.nodes;
import src.utils.Position;
import src.utils.Token;

public class VariableDeclarationNode extends ASTNode {
    private Token dataType;
    private Token identifier;
    private Token literal;
    private String value;

    public VariableDeclarationNode(Token dataType, Token identifier, Token literal) {
        super(dataType.getPosition());
        this.dataType = dataType;
        this.identifier = identifier;
        this.literal = literal;
        this.value = literal.getLexeme();
    }

    public VariableDeclarationNode(Token dataType, Token identifier) {
        super(dataType.getPosition());
        this.dataType = dataType;
        this.identifier = identifier;
        this.literal = null;
    }

    // Getters for data type, variable name, and value
    public Token.Type getType() {
        return dataType.getType();
    }

    public String getName() {
        return identifier.getLexeme();
    }

    public String getValue() {
        return literal.getLexeme();
    }

    @Override
    public String toString() {
        if (value != null) {
            return String.format("%s %s = %s", dataType, identifier.getLexeme(), literal.getLexeme(););
        } else {
            return String.format("%s %s", dataType, identifier.getLexeme());
        }
    }
}