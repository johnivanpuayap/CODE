package src.nodes;

import java.util.List;
import src.utils.Position;

public class FunctionNode extends StatementNode {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FunctionNode{name=").append(functionName).append(", arguments=[");
        
        // Append each argument
        for (int i = 0; i < arguments.size(); i++) {
            sb.append(arguments.get(i));
            if (i < arguments.size() - 1) {
                sb.append(" "); // Add a comma if it's not the last argument
            }
        }
        
        sb.append("]}");
        return sb.toString();
    }
}