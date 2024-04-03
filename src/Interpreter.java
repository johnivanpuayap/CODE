package src;

import java.util.Map;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import src.nodes.DeclarationNode;
import src.nodes.ProgramNode;
import src.nodes.StatementNode;
import src.nodes.ExpressionNode;
import src.utils.Variable;
import src.utils.Token;

public class Interpreter {
    private Map<String, Variable> variables = new HashMap<>();
    private ProgramNode program;
    private List<DeclarationNode> declarations;
    private List<StatementNode> statements;

     public Interpreter(ProgramNode program) {
        this.program = program;
        declarations = this.program.getDeclarations();
        statements = this.program.getStatements();
    }

    public void interpret() {

        for(DeclarationNode declaration: declarations) {
            variables.put(declaration.getVariableName(), new Variable(declaration.getDataType(), declaration.getValue(), declaration.getPosition()));
        }
        

        for (StatementNode statement : statements) {
            interpretStatement(statement);
        }
    }

    private void interpretStatement(StatementNode statement) {

        Token leftSide = statement.getLeftSide();
        

        if (statement.hasExpression()) {

            Variable var = variables.get(leftSide.getValue());
            ExpressionNode expression = statement.getExpressionNode();

            System.out.println("Variable: " + var);

            String result = evaluateExpression(expression);
            var.setValue(result);

            System.out.println("Evaluated " + " expression: " + expression);
            System.out.println("Result " + result);

        } else {
            Variable left = variables.get(leftSide.getValue());
            Token rightSide = statement.getRightSide();

            if(rightSide.getType() == Token.Type.VARIABLE) {
                Variable right = variables.get(rightSide.getValue());

                left.setValue(right.getValue());

                System.out.println("Assign to " + leftSide.getValue() + " the number: " + right.getValue());

            } else {
                left.setValue(rightSide.getValue());
                System.out.println("Assign to " + leftSide.getValue() + " the number: " + rightSide.getValue());
            }
        }
    }

    private String evaluateExpression(ExpressionNode expression) {
        
        List<Token> prefixTokens = expression.getTokens();
        List<Token> postfixTokens = convertInfixToPostfix(prefixTokens);
        
        for (Token token : prefixTokens) {
            System.out.println("Values: " + token.getValue());
        }
        
        for (Token token: postfixTokens) {
            System.out.println("Values: " + token.getValue());
        }

        double result = evaluatePostfix(postfixTokens);


        return String.valueOf(result);
    }

    public List<Token> convertInfixToPostfix(List<Token> infixTokens) {
        Stack<Token> stack = new Stack<>();
        List<Token> postfixTokens = new ArrayList<>();
    
        for (Token token : infixTokens) {
            if (token.getType() == Token.Type.VALUE || token.getType() == Token.Type.VARIABLE) {
                postfixTokens.add(token);
            } else if (token.getValue().equals("(")) {
                stack.push(token);
            } else if (token.getValue().equals(")")) {
                while (!stack.isEmpty() && !stack.peek().getValue().equals("(")) {
                    postfixTokens.add(stack.pop());
                }
                if (!stack.isEmpty()){
                    stack.pop();
                }
            } else {
                while (!stack.isEmpty() && stack.peek().getType() == Token.Type.OPERATOR && precedence(token) <= precedence(stack.peek())) {
                    postfixTokens.add(stack.pop());
                }
                stack.push(token);
            }
        }
    
        // Pop remaining operators from the stack and add them to postfixTokens
        while (!stack.isEmpty()) {
            postfixTokens.add(stack.pop());
        }
    
        return postfixTokens;
    }
    
    private int precedence(Token token) {
        switch (token.getValue()) {
            case "+":
            case "-":
                return 0;
            case "*":
            case "/":
                return 1;
            case "(":
            case ")":
                return 2;
            default:
                return -1;
        }
    }
    
    public double evaluatePostfix(List<Token> postfixTokens) {
        Stack<Double> stack = new Stack<>();
    
        for (Token token : postfixTokens) {
            if (token.getType() == Token.Type.OPERATOR) {
                double operand2 = stack.pop();
                double operand1 = stack.pop();
                double result = applyOperator(token.getValue(), operand1, operand2);
                stack.push(result);
            } else if(token.getType() == Token.Type.VARIABLE) {
                Variable var = variables.get(token.getValue());
                stack.push(Double.parseDouble(var.getValue()));
            }  
            else { // Assuming it's a numeric value
                stack.push(Double.parseDouble(token.getValue()));
            }
        }
    
        return stack.pop();
    }

    private double applyOperator(String operator, double operand1, double operand2) {
        switch (operator) {
            case "+":
                return operand1 + operand2;
            case "-":
                return operand1 - operand2;
            case "*":
                return operand1 * operand2;
            case "/":
                return operand1 / operand2;
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }
}