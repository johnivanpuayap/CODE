package src.nodes;

import src.utils.Token;

public abstract class ExpressionNode {

    public abstract int countTokens();

    public static class Binary extends ExpressionNode {
        private final Token operator;
        private final ExpressionNode left;
        private final ExpressionNode right;

        public Binary(Token operator, ExpressionNode left, ExpressionNode right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        public Token getOperator() {
            return operator;
        }

        public ExpressionNode getLeft() {
            return left;
        }

        public ExpressionNode getRight() {
            return right;
        }

        @Override
        public String toString() {
            return "(" + left + " " + operator.getValue() + " " + right + ")";
        }

        @Override
        public int countTokens() {
            return left.countTokens() + right.countTokens() + 1; // Add 1 for the operator token
        }
    }

    public static class Literal extends ExpressionNode {
        private final Token value;

        public Literal(Token value) {
            this.value = value;
        }

        public Token getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value.getValue();
        }

        @Override
        public int countTokens() {
            return 1; // Literal node has only one token
        }
    }

    public static class Variable extends ExpressionNode {
        private final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        public Token getName() {
            return name;
        }

        @Override
        public String toString() {
            return name.getValue();
        }

        @Override
        public int countTokens() {
            return 1; // Variable node has only one token
        }
    }
}