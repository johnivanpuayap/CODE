import java.util.List;

class ProgramNode extends ASTNode {

    private List<StatementNode> statements;

    public ProgramNode(List<StatementNode> statements) {
        this.statements = statements;
    }
}