import java.util.List;

class ProgramNode extends ASTNode {
    private List<DeclarationNode> declarations;
    private List<StatementNode> statements;

    public ProgramNode(List<DeclarationNode> declarations, List<StatementNode> statements) {
        this.declarations = declarations;
        this.statements = statements;
    }

    // Getters for accessing the private fields
    public List<DeclarationNode> getDeclarations() {
        return declarations;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }
}