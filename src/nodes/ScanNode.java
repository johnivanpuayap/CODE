package src.nodes;

import java.util.List;
import src.utils.Position;
import src.utils.Token;

public class ScanNode extends StatementNode {
    private List<Token> identifiers;

    public ScanNode(List<Token> identifiers, Position position) {
        super(position);
        this.identifiers = identifiers;
    }

    public List<Token> getIdentifiers() {
        return identifiers;
    }
}