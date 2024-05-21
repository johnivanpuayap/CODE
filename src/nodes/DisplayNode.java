package src.nodes;

import java.util.List;
import src.utils.Token;

public class DisplayNode extends StatementNode {
    private List<Token> arguments;

    public DisplayNode(List<Token> arguments) {
        super(arguments.getFirst().getPosition());
        this.arguments = arguments;
    }

    public List<Token> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DisplayNode { expressions = [");

        // Append each expression
        for (int i = 0; i < arguments.size(); i++) {

            sb.append(arguments.get(i));

            if (i < arguments.size() - 1) {
                sb.append("& "); // Add a concatenation symbol if it's not the last expression
            }
        }

        sb.append("]}");
        return sb.toString();
    }
}