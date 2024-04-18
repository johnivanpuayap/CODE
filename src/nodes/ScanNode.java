package src.nodes;

import java.util.List;

public class ScanNode extends StatementNode {
    private List<String> identifiers;

    public ScanNode(List<String> identifiers) {
        super(null);
        this.identifiers = identifiers;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

}