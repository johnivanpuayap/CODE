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
import src.utils.SymbolTable;
import src.utils.Symbol;


public class Interpreter {
    private ProgramNode program;
    private SymbolTable symbolTable;

    public Interpreter(ProgramNode program, SymbolTable symbolTable) {
        this.program = program;
        this.symbolTable = symbolTable;
    }
    

    public void interpret() {
        List<StatementNode> statements = program.getStatements();


        System.out.println("\n\n\n\n\nPROGRAM RESULTS");


        for (StatementNode statement : statements) {
            interpretStatement(statement);
        }
    }

    private void interpretStatement(StatementNode statement) {

        if(statement instanceof AssignmentNode) {
            
            AssignmentNode assignment = (AssignmentNode) statement;
            if (assignment.getExpression() instanceof LiteralNode) {
               
                
            } else if (assignment.getExpression() instanceof UnaryNode) {
                Symbol s = symbolTable.lookup(assignment.getVariable().getName());
                
                if (s.getType() != Type.FLOAT || s.getType() != Type.INT) {
                    error("Type mismatch. Assigning a Number to a ", assignment.getVariable().getPosition());
                }

                s.setValue(assignment.getExpression().toString());


            } else if(assignment.getExpression() instanceof VariableNode) {

                Symbol left = symbolTable.lookup(assignment.getVariable().getName());
                Symbol right = symbolTable.lookup(((VariableNode) assignment.getExpression()).getName());

                left.setValue(right.getValue());
            } else {

                double result = evaluateExpression((ExpressionNode) assignment.getExpression());
                
                String value = String.valueOf(result);

                Symbol symbol = symbolTable.lookup(assignment.getVariable().getName());

                if(symbol.getType() == Type.INT) {
                    if(value.contains(".")) {
                        String newValue = value.substring(0, value.indexOf("."));
                        value = newValue;
                    }
                }
                
                symbol.setValue(value);
                
            }
        } else if(statement instanceof DisplayNode) {
            interpretDisplay((DisplayNode) statement);
        } else if(statement instanceof ScanNode) {
            interpretScan((ScanNode) statement);
        }
        
    }

    public double evaluateExpression(ExpressionNode expression) {

        System.out.println("Expression: " + expression.toString());

        List<Token> tokens = expression.getTokens();
       
        System.out.println("Tokens: " + tokens);

        List<String> postfixExpression = infixToPostfix(tokens);

        return evaluatePostfix(postfixExpression);
    }

    private List<String> infixToPostfix(List<Token> tokens) {
        Stack<String> operatorStack = new Stack<>();
        List<String> postfix = new ArrayList<>();
    
        for (Token token : tokens) {
            
            System.out.println("Token: " + token.getLexeme());
    
            if (token.getType() == Type.IDENTIFIER || token.getType() == Type.LITERAL) {
                postfix.add(token.getLexeme());
            } else if (token.getLexeme().equals("(")) {
                operatorStack.push(token.getLexeme());
            } else if (token.getLexeme().equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    postfix.add(operatorStack.pop());
                }
                operatorStack.pop();
            } else {
                while (!operatorStack.isEmpty() && hasHigherPrecedence(operatorStack.peek(), token.getLexeme())) {
                    postfix.add(operatorStack.pop());
                }
                operatorStack.push(token.getLexeme());
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
            } else if (symbolTable.lookup(token) != null) {
                stack.push(Double.parseDouble(symbolTable.lookup(token).getValue()));
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
                
                Symbol symbol = symbolTable.lookup(token.getLexeme());

                String value = symbol.getValue();

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
    
        for (Token identifier : scanStatement.getIdentifiers()) {
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
        }
    
        scanner.close();
    }

    private VariableDeclarationNode findVariableDeclaration(String identifier) {
        for (VariableDeclarationNode declaration : declarations) {
            if (declaration.getVariableName().equals(identifier)) {
                return declaration;
            }
        }
        return null; // Return null if variable declaration not found
    }

    private boolean isCompatible(String variableType, String inputType) {
        if (variableType.equals("INT") && (inputType.equals("INT") || inputType.equals("FLOAT"))) {
            return true;
        } else if (variableType.equals("FLOAT") && (inputType.equals("INT") || inputType.equals("FLOAT"))) {
            return true;
        } else if (variableType.equals("CHAR") && inputType.equals("CHAR")) {
            return true;
        } else if (variableType.equals("BOOL") && inputType.equals("BOOL")) {
            return true;
        } else {
            return false;
        }
    }

    private void error(String message, Position position) {
        System.err.println("Error: " + message + " " + position);
        System.exit(1);
    }
}