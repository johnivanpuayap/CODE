package src.nodes;

import java.util.ArrayList;
import java.util.List;

import src.utils.Position;
import src.utils.Token;

public abstract class ExpressionNode{

    public abstract int countTokens();
    public abstract List<Token> getTokens();
    public abstract Token getToken(int index);

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
            return "(" + left.toString() + operator.getValue() + right.toString() + ")";
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
        public Token getToken(int index) {
            List<Token> tokens = getTokens();
            return tokens.get(index);
        }

        @Override
        public int countTokens() {
            return getTokens().size();
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
            List<Token> tokens = new ArrayList<>(1);
            tokens.add(name);
            return tokens;
        }

        @Override
        public Token getToken(int index) {
            List<Token> tokens = getTokens();
            return tokens.get(index);
        }
        
        @Override
        public int countTokens() {
            return 1; // Variables will always have one token
        }
    }

    public static class Unary extends ExpressionNode {
        private final OperatorNode operator;
        private final ExpressionNode operand;
    
        public Unary(OperatorNode operator, ExpressionNode operand) {

            // create the new List of tokens

            List<Token> tokens = new ArrayList<>();
            tokens.addAll(operator.getTokens());
            tokens.addAll(operand.getTokens());

            this.operator = operator;
            this.operand = operand;
        }
    
        public List<Token> getOperator() {
            return operator.getTokens();
        }
    
        public ExpressionNode getOperand() {
            return operand;
        }
    
        @Override
        public String toString() {
            return operator.getValue() + operand.toString();
        }
    
        @Override
        public int countTokens() {
            return operand.countTokens() + operator.countTokens(); // Add 1 for the operator token
        }
    
        @Override
        public List<Token> getTokens() {
            List<Token> tokens = new ArrayList<>();
            tokens.addAll(operator.getTokens());
            tokens.addAll(operand.getTokens());
            return tokens;
        }
    }

    public static class Operator extends ExpressionNode{

    }
}