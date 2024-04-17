package src.nodes;

import java.util.List;
import src.utils.Position;

public class FunctionCallNode extends ASTNode {
    private String functionName;
    private List<Token> arguments;

    public FunctionNode(String functionName, List<Token> arguments, Position position) {
        super(position);
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Token> getArguments() {
        return arguments;
    }

    public Position getPosition() {
        return super.getPosition();
    }
}