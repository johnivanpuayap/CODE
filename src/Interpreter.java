package src;

import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import src.nodes.ASTNode;
import src.nodes.AssignmentStatementNode;
import src.nodes.VariableDeclarationNode;
import src.nodes.ProgramNode;
import src.nodes.SpecialCharacterNode;
import src.nodes.StatementNode;
import src.nodes.StringLiteralNode;
import src.nodes.VariableNode;
import src.nodes.ExpressionNode;
import src.nodes.FunctionNode;
import src.utils.Variable;
import src.utils.Token;
import src.utils.Position;


public class Interpreter {
    private Map<String, Variable> variables = new HashMap<>();
    private ProgramNode program;
    private List<VariableDeclarationNode> declarations;
    private List<StatementNode> statements;

     public Interpreter(ProgramNode program) {
        this.program = program;
        declarations = this.program.getDeclarations();
        statements = this.program.getStatements();
    }

    public void interpret() {

        System.out.println("\n\n\n\n\nPROGRAM RESULTS");


        for(VariableDeclarationNode declaration: declarations) {
            variables.put(declaration.getVariableName(), new Variable(declaration.getDataType(), declaration.getValue(), declaration.getPosition()));
        }

        System.out.println(variables);

        for (StatementNode statement : statements) {
            interpretStatement(statement);
        }

        System.out.println(variables);
    }

    private void interpretStatement(StatementNode statement) {

        if(statement instanceof AssignmentStatementNode) {
            
            AssignmentStatementNode assignment = (AssignmentStatementNode) statement;
            if (assignment.getExpression() instanceof ExpressionNode.Literal ||
                assignment.getExpression() instanceof ExpressionNode.Unary) {
                Variable var = variables.get(assignment.getVariable().getVariableName());
                
                var.setValue(assignment.getExpression().toString());
            } else if(assignment.getExpression() instanceof ExpressionNode.Variable) {
                Variable var = variables.get(assignment.getVariable().getVariableName());
                Variable var2 = variables.get(((ExpressionNode.Variable) assignment.getExpression()).getName().getValue());

                if (var2.getDataType() != var.getDataType()) {
                    error("Type mismatch", var2.position());
                }

                var.setValue(var2.getValue());
            } else {
                System.out.println("Evaluating expression");
                
                System.out.println("Assignment: " + assignment);

                Variable var = variables.get(assignment.getVariable().getVariableName());
                double result = evaluateExpression((ExpressionNode) assignment.getExpression());


                String value = String.valueOf(result);


                if (var.getDataType().equals("INT") && value.endsWith(".0")) {
                    var.setValue(value.substring(0, value.length() -2));    
                } else if(var.getDataType().equals("FLOAT")){
                    var.setValue(value);
                } else {
                    error("Type mismatch. Assigning a number to a " + var.getDataType() + " data type", var.position());
                }
                
            }
        } else if(statement instanceof FunctionNode) {
            interpretFunction((FunctionNode) statement);
        }
        
    }

    public double evaluateExpression(ExpressionNode expression) {

        List<String> tokens = tokenize(expression.toString());

        System.out.println(tokens);

        List<String> postfixExpression = infixToPostfix(tokens);

        System.out.println(postfixExpression);

        return evaluatePostfix(postfixExpression);
    }

    public List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?<=^|[-+*/])[+-]?\\d+|[-+*/()]|[a-zA-Z]+");
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
    
        System.out.println(tokens);
    
        return tokens;
    }

    private List<String> infixToPostfix(List<String> tokens) {
        Stack<String> operatorStack = new Stack<>();
        List<String> postfix = new ArrayList<>();
    
        for (String token : tokens) {
    
            if (token.matches("[a-zA-Z]+") || token.matches("[-+]?[0-9]+")) {
                postfix.add(token);
            } else if (token.equals("(")) {
                operatorStack.push(token);
            } else if (token.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    postfix.add(operatorStack.pop());
                }
                operatorStack.pop();
            } else {
                while (!operatorStack.isEmpty() && hasHigherPrecedence(operatorStack.peek(), token)) {
                    postfix.add(operatorStack.pop());
                }
                operatorStack.push(token);
            }
        }
    
        while (!operatorStack.isEmpty()) {
            postfix.add(operatorStack.pop());
        }
    
        return postfix;
    }
    
    public double evaluatePostfix(List<String> postfixExpression) {
        Stack<Double> stack = new Stack<>();
    
        for (String token : postfixExpression) {
            if (token.matches("[-+]?[0-9]+")) {
                stack.push(Double.valueOf(token));
            } else if (variables.containsKey(token)) {
                stack.push(Double.parseDouble(variables.get(token).getValue()));
            } else {
                double operand2 = stack.pop();
                double operand1 = stack.pop();
    
                switch (token) {
                    case "+":
                        stack.push(operand1 + operand2);
                        break;
                    case "-":
                        stack.push(operand1 - operand2);
                        break;
                    case "*":
                        stack.push(operand1 * operand2);
                        break;
                    case "/":
                        if (operand2 != 0.0) {
                            stack.push(operand1 / operand2);
                        } else {
                            throw new IllegalArgumentException("Cannot divide by zero");
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid operator: " + token);
                }
            }
        }
    
        return stack.pop();
    }

    private boolean hasHigherPrecedence(String a, String d) {
        int precedence1 = getOperatorPrecedence(a);
        int precedence2 = getOperatorPrecedence(d);
        return precedence1 >= precedence2;
    }
    
    private int getOperatorPrecedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            default:
                return 0;
        }
    }
    
    private void interpretFunction(FunctionNode function) {   
        
        if (function.getFunctionName() == "DISPLAY") {
            for (ASTNode node : function.getArguments()) {
                if (node instanceof StringLiteralNode) {
                    StringLiteralNode stringLiteral = (StringLiteralNode) node;
                    System.out.print(stringLiteral.getValue());
                    continue;
                }
                if (node instanceof VariableNode) {
                    VariableNode variable = (VariableNode) node;

                    Variable var = variables.get(variable.getVariableName());
                    System.out.print(var.getValue());
                    continue;
                }
                if (node instanceof SpecialCharacterNode) {
                    SpecialCharacterNode specialCharacter = (SpecialCharacterNode) node;
                    System.out.print(specialCharacter.getValue());
                }
            }
        } else if (function.getFunctionName() == "SCAN") {

        }
            
    }


    private void error(String message, Position position) {
        System.err.println("Error: " + message + " " + position);
        System.exit(1);
    }
}