package src;

import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import src.nodes.*;
import src.utils.Token;
import src.utils.Type;
import src.utils.Position;
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

        if (statement instanceof AssignmentNode) {

            AssignmentNode assignment = (AssignmentNode) statement;
            if (assignment.getExpression() instanceof LiteralNode) {

            } else if (assignment.getExpression() instanceof UnaryNode) {
                Symbol s = symbolTable.lookup(assignment.getVariable().getName());

                if (s.getType() != Type.FLOAT || s.getType() != Type.INT) {
                    error("Type mismatch. Assigning a Number to a ", assignment.getVariable().getPosition());
                }

                s.setValue(assignment.getExpression().toString());

            } else if (assignment.getExpression() instanceof VariableNode) {

                Symbol left = symbolTable.lookup(assignment.getVariable().getName());
                Symbol right = symbolTable.lookup(((VariableNode) assignment.getExpression()).getName());

                left.setValue(right.getValue());
            } else {

                double result = evaluateExpression((ExpressionNode) assignment.getExpression());

                String value = String.valueOf(result);

                Symbol symbol = symbolTable.lookup(assignment.getVariable().getName());

                if (symbol.getType() == Type.INT) {
                    if (value.contains(".")) {
                        String newValue = value.substring(0, value.indexOf("."));
                        value = newValue;
                    }
                }

                symbol.setValue(value);

            }
        } else if (statement instanceof DisplayNode) {
            interpretDisplay((DisplayNode) statement);
        } else if (statement instanceof ScanNode) {
            interpretScan((ScanNode) statement);
        }

    }

    public double evaluateExpression(ExpressionNode expression) {

        System.out.println("Expression: " + expression.toString());

        List<Token> tokens = expression.getTokens();

        List<Token> postfixExpression = infixToPostfix(tokens);

        return evaluatePostfix(postfixExpression);
    }

    private List<Token> infixToPostfix(List<Token> tokens) {
        Stack<Token> operatorStack = new Stack<>();
        List<Token> postfix = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {

            Token token = tokens.get(i);

            if (token.getType() == Type.IDENTIFIER || token.getType() == Type.LITERAL) {
                postfix.add(token);
            } else if (token.getType() == Type.NEGATIVE
                    || token.getType() == Type.POSITIVE) {

                postfix.add(new Token(Type.LITERAL, token.getLexeme() + tokens.get(i + 1).getLexeme(),
                        token.getPosition()));
                i++;

            } else if (token.getLexeme().equals("(")) {
                operatorStack.push(token);
            } else if (token.getLexeme().equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().getLexeme().equals("(")) {
                    postfix.add(operatorStack.pop());
                }
                operatorStack.pop();
            } else {
                while (!operatorStack.isEmpty()
                        && hasHigherPrecedence(operatorStack.peek().getLexeme(), token.getLexeme())) {
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

    public double evaluatePostfix(List<Token> postfixExpression) {
        Stack<Double> stack = new Stack<>();

        for (int i = 0; i < postfixExpression.size(); i++) {

            Token token = postfixExpression.get(i);
            System.out.println("Evaluating " + token.getLexeme());

            String lexeme = token.getLexeme();

            if (lexeme.matches("[-+]?[0-9]+")) {
                stack.push(Double.valueOf(lexeme));
            } else if (symbolTable.lookup(lexeme) != null) {
                stack.push(Double.parseDouble(symbolTable.lookup(lexeme).getValue()));
            } else {
                double operand2 = stack.pop();
                double operand1 = stack.pop();

                switch (lexeme) {
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
                            error("Cannot divide by zero", token.getPosition());
                        }
                        break;
                    case "%":
                        stack.push(operand1 % operand2);
                        break;
                    default:
                        error("Cannot divide by zero", token.getPosition());
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
            case "%":
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
            System.out.print(identifier.getLexeme() + ": ");
            String input = scanner.nextLine();

            // Convert to a Data Type

            Type inputDataType = null;

            if (input.matches("[-+]?[0-9]+")) {
                inputDataType = Type.INT;
            } else if (input.matches("[-+]?[0-9]+(\\.[0-9]+)?")) {
                inputDataType = Type.FLOAT;
            } else if (input.matches("[a-zA-Z]")) {
                inputDataType = Type.CHAR;
            } else if (input.equals("TRUE") || input.equals("FALSE")) {
                inputDataType = Type.BOOL;
            } else {
                error("Invalid input", null);
            }

            Symbol symbol = symbolTable.lookup(identifier.getLexeme());

            if (symbol.getType() != inputDataType) {
                error("Type mismatch. Assigning a " + inputDataType + " to a " + symbol.getType() + " datatype",
                        scanStatement.getPosition());
            }

            symbol.setValue(input);
        }

        scanner.close();
    }

    private void error(String message, Position position) {
        System.err.println("Error: " + message + " " + position);
        System.exit(1);
    }
}