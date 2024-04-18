package src;

import java.util.Map;
import java.util.Stack;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Scanner;

import src.nodes.*;
import src.utils.Token;
import src.utils.Type;
import src.utils.Position;
import src.utils.Variable;


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
            variables.put(declaration.getName(), new Variable(declaration.getType(), declaration.getValue(), declaration.getPosition()));
        }

        for (StatementNode statement : statements) {
            interpretStatement(statement);
        }
    }

    private void interpretStatement(StatementNode statement) {

        if(statement instanceof AssignmentNode) {
            
            AssignmentNode assignment = (AssignmentNode) statement;
            if (assignment.getExpression() instanceof LiteralNode ||
                assignment.getExpression() instanceof UnaryNode) {
                Variable var = variables.get(assignment.getVariable().getName());
                
                var.setValue(assignment.getExpression().toString());
            } else if(assignment.getExpression() instanceof VariableNode) {
                Variable var = variables.get(assignment.getVariable().getName());
                Variable var2 = variables.get(((VariableNode) assignment.getExpression()).getName());

                if (var2.getDataType() != var.getDataType()) {
                    error("Type mismatch", var2.position());
                }

                var.setValue(var2.getValue());
            } else {

                Variable var = variables.get(assignment.getVariable().getName());
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
        } else if(statement instanceof DisplayNode) {
            interpretDisplay((DisplayNode) statement);
        } else if(statement instanceof ScanNode) {
            interpretScan((ScanNode) statement);
        }
        
    }

    public double evaluateExpression(ExpressionNode expression) {

        List<String> tokens = tokenize(expression.toString());

        List<String> postfixExpression = infixToPostfix(tokens);

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
    
    private void interpretDisplay(DisplayNode display) {   
                
        for (Token token : display.getArguments()) {
            if (token.getType() == Type.STRING_LITERAL) {
                System.out.print(token.getLexeme());
                continue;
            }
            if (token.getType() == Type.IDENTIFIER) {
                
                Variable var = variables.get(token.getLexeme());

                String value = var.getValue();

                System.out.print(value);
                continue;
            }
            if (token.getType() == Type.SPECIAL_CHARACTER) {
                System.out.print(token.getLexeme());
            }
        }
 
    }

    private void interpretScan(ScanNode scanStatement) {
        Scanner scanner = new Scanner(System.in);
    
        for (String identifier : scanStatement.getIdentifiers()) {
            System.out.print(identifier + ": ");
            String input = scanner.nextLine();

            // Convert to a Data Type

            String inputDataType = null;

            if(input.matches("[-+]?[0-9]+")) {
                inputDataType = "INT";
            } else if(input.matches("[-+]?[0-9]+(\\.[0-9]+)?")) {
                inputDataType = "FLOAT";
            } else if(input.matches("[a-zA-Z]")) {
                inputDataType = "CHAR";
            } else if(input.equalsIgnoreCase("TRUE") || input.equalsIgnoreCase("FALSE")) {
                inputDataType = "BOOL";
            } else {
                error("Invalid input", null);
            }
    
            for(VariableDeclarationNode declaration: declarations) {
                if(declaration.getName().equals(identifier)) {
                    if(declaration.getType().equals(inputDataType)) {
                        variables.get(identifier).setValue(input);
                    } else {
                        error("Type mismatch. Assigning " + inputDataType + " to " + declaration.getType(), declaration.getPosition());
                    }
                }
            
            }
        }
    
        scanner.close();
    }
    
    private void error(String message, Position position) {
        System.err.println("Error: " + message + " " + position);
        System.exit(1);
    }
}