package src.nodes;
import java.util.List;
import src.utils.Position;

public class DisplayNode extends StatementNode {
    private List<ExpressionNode> expressions;

    public DisplayNode(List<ExpressionNode> expressions, Position position) {
        super(position);
        this.expressions = expressions;
    }

    public List<ExpressionNode> getExpressions() {
        return expressions;
    }

    public Position getPosition() {
        return super.getPosition();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DisplayNode{expressions=[");
        
        // Append each expression
        for (int i = 0; i < expressions.size(); i++) {
            sb.append(expressions.get(i));
            if (i < expressions.size() - 1) {
                sb.append(" & "); // Add a concatenation symbol if it's not the last expression
            }
        }
        
        sb.append("]}");
        return sb.toString();
    }
}