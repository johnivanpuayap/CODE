package src.nodes;
import java.util.List;

public class ProgramNode {
    private List<DeclarationNode> declarations;
    private List<StatementNode> statements;
    private List<FunctionNode> functionCalls;

    public ProgramNode(List<DeclarationNode> declarations, List<StatementNode> statements, List<FunctionNode> functionCalls) {
        this.declarations = declarations;
        this.statements = statements;
        this.functionCalls = functionCalls;
    }

    // Getters for accessing the private fields
    public List<DeclarationNode> getDeclarations() {
        return declarations;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    public List<FunctionNode> getFunctionCalls() {
        return functionCalls;
    }
}