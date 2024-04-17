package src.nodes;

import java.util.List;
import src.utils.Position;

public class FunctionNode extends ASTNode {
    private String functionName;
    private List<ASTNode> arguments;

    public FunctionNode(String functionName, List<ASTNode> arguments, Position position) {
        super(position);
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<ASTNode> getArguments() {
        return arguments;
    }

    public Position getPosition() {
        return super.getPosition();
    }
}

