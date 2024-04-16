package src.nodes;
import java.util.List;

public class ProgramNode {
    private List<VariableDeclarationNode> declarations;
    private List<StatementNode> statements;
    private List<FunctionNode> functionCalls;

    public ProgramNode(List<VariableDeclarationNode> declarations, List<StatementNode> statements) {
        this.declarations = declarations;
        this.statements = statements;
    }

    // Getters for accessing the private fields
    public List<VariableDeclarationNode> getDeclarations() {
        return declarations;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Print variable declarations
        sb.append("Variable Declarations:\n");
        for (VariableDeclarationNode declaration : declarations) {
            sb.append(declaration).append("\n");
        }
        
        // Print statements
        sb.append("\nStatements:\n");
        for (StatementNode statement : statements) {
            sb.append(statement).append("\n");
        }
        
        return sb.toString();
    }
}