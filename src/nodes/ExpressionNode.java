package src.nodes;

import java.util.List;
import src.utils.Position;
import src.utils.Token;

public abstract class ExpressionNode extends ASTNode{

    public abstract int countTokens();
    public abstract List<Token> getTokens();
    public abstract Token getToken(int index);

    public ExpressionNode(Position position) {
        super(position);
    }
}