package src.nodes;

import src.utils.Position;

public abstract class ASTNode {
    private Position position;
    // Implement common AST node methods here

    public ASTNode(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}