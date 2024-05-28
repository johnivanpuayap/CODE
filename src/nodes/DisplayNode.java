package src.nodes;

import java.util.List;
import src.utils.Token;
import src.utils.Type;
import src.nodes.ExpressionNode;

public class DisplayNode extends StatementNode {
    private List<Token> arguments;
    private List<ExpressionNode> expressions;

    public DisplayNode(List<Token> arguments, List<ExpressionNode> expressions) {
        super(arguments.getFirst().getPosition());
        this.arguments = arguments;
        this.expressions = expressions;
    }

    public List<Token> getArguments() {
        return arguments;
    }

    public List<ExpressionNode> getExpressions() {
        return expressions;
    }

    public ExpressionNode getExpression(int i) {
        return expressions.get(i);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DisplayNode { arguments = [");

        // Append each arguments
        int expressionCounter = 0;
        for (int i = 0; i < arguments.size(); i++) {

            if (arguments.get(i).getType() == Type.EXPRESSION) {
                for (Token token : expressions.get(expressionCounter).getTokens()) {
                    sb.append(token + " ");
                }
                expressionCounter++;
                continue;
            }

            sb.append(arguments.get(i) + " ");

            if (i < arguments.size() - 1) {
                sb.append(" & "); // Add a concatenation symbol if it's not the last expression
            }
        }

        sb.append("]}");
        return sb.toString();
    }
}