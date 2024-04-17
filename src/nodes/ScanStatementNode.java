package src.nodes;

import java.util.List;

public class ScanStatementNode extends StatementNode {
    private List<String> identifiers;

    public ScanStatementNode(List<String> identifiers) {
        super(null);
        this.identifiers = identifiers;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

}