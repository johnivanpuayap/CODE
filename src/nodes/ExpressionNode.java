package src.nodes;

import java.util.ArrayList;
import java.util.List;

import src.utils.Position;
import src.utils.Token;

public abstract class ExpressionNode{

    public abstract int countTokens();
    public abstract List<Token> getTokens();

    public static class Binary extends ExpressionNode {
        private final Token operator;
        private final ExpressionNode left;
        private final ExpressionNode right;

        public Binary(ExpressionNode left, Token operator, ExpressionNode right) {
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

        @Override
        public List<Token> getTokens() {
            List<Token> tokens = new ArrayList<>();
            tokens.add(new Token(Token.Type.RIGHT_PARENTHESIS, "(", new Position(0, 0))); // Add opening parenthesis
            tokens.addAll(left.getTokens());
            tokens.add(operator);
            tokens.addAll(right.getTokens());
            tokens.add(new Token(Token.Type.LEFT_PARENTHESIS, ")", new Position(0, 0))); // Add closing parenthesis
            return tokens;
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

            System.out.println("Value: " + value.getValue());

            return value.getValue();
        }

        @Override
        public List<Token> getTokens() {
            List<Token> tokens = new ArrayList<>();
            tokens.add(value);
            return tokens;
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
        public List<Token> getTokens() {
            List<Token> tokens = new ArrayList<>();
            tokens.add(name);
            return tokens;
        }
        
        @Override
        public int countTokens() {
            return 1; // Variable node has only one token
        }
    }
}