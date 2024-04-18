package src.nodes;

import java.util.ArrayList;
import java.util.List;
import src.utils.Position;
import src.utils.Token;
import src.utils.Type;

public abstract class ExpressionNode{

    public abstract int countTokens();
    public abstract List<Token> getTokens();
    public abstract Token getToken(int index);

    public static class Binary extends ExpressionNode {
        private final Token operator;
        private final ExpressionNode left;
        private final ExpressionNode right;
        private final List<Token> tokens = new ArrayList<>();

        public Binary(Token operator, ExpressionNode left, ExpressionNode right) {
            this.operator = operator;
            this.left = left;
            this.right = right;

            List<Token> tokens = new ArrayList<>();
            tokens.add(new Token(Type.RIGHT_PARENTHESIS, "(", new Position(0, 0)));
            tokens.addAll(left.getTokens());
            tokens.add(operator);
            tokens.addAll(right.getTokens());
            tokens.add(new Token(Type.LEFT_PARENTHESIS, ")", new Position(0, 0)));
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
            return "(" + left.toString() + operator.toString() + right.toString() + ")";
        }


        @Override
        public int countTokens() {
            return left.countTokens() + right.countTokens() + 1; // Add 1 for the operator token
        }

        @Override
        public Token getToken(int index) {
            List<Token> tokens = getTokens();
            return tokens.get(index);
        }

        @Override
        public List<Token> getTokens() {
            return tokens;
        }
    }

    public static class Literal extends ExpressionNode {
        private final Token value;
        private final List<Token> tokens = new ArrayList<>(1);

        public Literal(Token value) {
            this.value = value;
            tokens.add(value);
        }

        public Token getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value.getLexeme();
        }

        @Override
        public List<Token> getTokens() {
            return tokens;
        }

        @Override
        public Token getToken(int index) {
            return tokens.get(index);
        }

        @Override
        public int countTokens() {
            return 1;
        }
    }

    public static class Variable extends ExpressionNode {
        private final Token name;
        private final List<Token> tokens = new ArrayList<>(1);

        public Variable(Token name) {
            this.name = name;
            tokens.add(name);
        }

        public Token getName() {
            return name;
        }

        @Override
        public String toString() {
            return name.getLexeme();
        }

        @Override
        public List<Token> getTokens() {
            return tokens;
        }

        @Override
        public Token getToken(int index) {
            return tokens.get(index);
        }
        
        @Override
        public int countTokens() {
            return 1; // Variables will always have one token
        }
    }

    public static class Unary extends ExpressionNode {
        private final Token operator;
        private final ExpressionNode operand;
        private final List<Token> tokens = new ArrayList<>();
    
        public Unary(Token operator, ExpressionNode operand) {
            this.operator = operator;
            this.operand = operand;

            tokens.add(operator);
            tokens.addAll(operand.getTokens());
        }
    
        public Token getOperator() {
            return operator;
        }
    
        public ExpressionNode getOperand() {
            return operand;
        }
    
        @Override
        public String toString() {
            return operator.getLexeme() + operand.toString();
        }
    
        @Override
        public int countTokens() {
            return operand.countTokens() + 1; // Add 1 for the operator token
        }
    
        @Override
        public List<Token> getTokens() {
            return tokens;
        }

        @Override
        public Token getToken(int index) {
            return tokens.get(index);
        }
    }
}