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

        for (int i = 0; i < statements.size(); i++) {

            StatementNode statement = statements.get(i);

            if (statement instanceof IfNode) {
                List<StatementNode> ifStatements = new ArrayList<>();

                ifStatements.add(statement);

                while (i + 1 < statements.size() && statements.get(i + 1) instanceof ElseIfNode) {
                    ifStatements.add(statements.get(i + 1));
                    i++;
                }

                if (i + 1 < statements.size() && statements.get(i + 1) instanceof ElseNode) {
                    ifStatements.add(statements.get(i + 1));
                    i++;
                }

                interpretIf(ifStatements);

            } else {
                interpretStatement(statement);
            }
        }
    }

    private void interpretStatement(StatementNode statement) {

        if (statement instanceof AssignmentNode) {

            AssignmentNode assignment = (AssignmentNode) statement;
            if (assignment.getExpression() instanceof LiteralNode) {

                Symbol s = symbolTable.lookup(assignment.getVariable().getName());

                LiteralNode literal = (LiteralNode) assignment.getExpression();

                if (s.getType() != literal.getDataType()) {
                    error("Type mismatch. Assigning a " + literal.getDataType() + " to a " + s.getType(),
                            assignment.getVariable().getPosition());
                }

                s.setValue(literal.toString());

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
        } else if (statement instanceof WhileNode) {
            interpretWhile((WhileNode) statement);
        } else if (statement instanceof ForNode) {
            interpretFor((ForNode) statement);
        }
    }

    public double evaluateExpression(ExpressionNode expression) {

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

        System.out.println();

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

    private void interpretIf(List<StatementNode> ifStatements) {

        StatementNode firstStatement = ifStatements.get(0);

        if (firstStatement instanceof IfNode) {
            IfNode ifNode = (IfNode) firstStatement;

            ExpressionNode condition = ifNode.getCondition();
            List<StatementNode> ifBranchStatements = ifNode.getStatements();

            boolean conditionResult = evaluateCondition(condition);

            if (conditionResult) {
                // Execute the statements in the current branch if the condition is true
                for (StatementNode branchStatement : ifBranchStatements) {
                    interpretStatement(branchStatement);
                }
                return;
            }

            if (ifStatements.size() > 1 && !ifStatements.isEmpty()) {
                ifStatements.remove(0);
                interpretIf(ifStatements);
            }

        } else if (firstStatement instanceof ElseIfNode) {
            ElseIfNode elseIfNode = (ElseIfNode) firstStatement;

            ExpressionNode condition = elseIfNode.getCondition();
            List<StatementNode> ifElseBranchStatements = elseIfNode.getStatements();

            boolean conditionResult = evaluateCondition(condition);

            if (conditionResult) {

                for (StatementNode branchStatement : ifElseBranchStatements) {
                    interpretStatement(branchStatement);
                }
                return;
            }

            // If none of the conditions are true, execute the statements in the final
            // "else" branch, if it exists
            if (ifStatements.size() > 0) {

                if (!ifStatements.isEmpty()) {
                    ifStatements.remove(0);
                    interpretIf(ifStatements);
                }
            }
        } else {
            ElseNode elseNode = (ElseNode) firstStatement;
            List<StatementNode> elseBranchStatements = elseNode.getStatements();

            for (StatementNode branchStatement : elseBranchStatements) {
                interpretStatement(branchStatement);
            }
        }
    }

    private void interpretWhile(WhileNode whileStatement) {
        ExpressionNode condition = whileStatement.getCondition();
        List<StatementNode> statements = whileStatement.getStatements();

        boolean result = evaluateCondition(condition);

        boolean breakFlag = false;

        while (result) {

            for (int i = 0; i < statements.size(); i++) {

                StatementNode statement = statements.get(i);

                if (statement instanceof IfNode) {
                    List<StatementNode> ifStatements = new ArrayList<>();

                    ifStatements.add(statement);

                    while (i + 1 < statements.size() && statements.get(i + 1) instanceof ElseIfNode) {
                        ifStatements.add(statements.get(i));
                        i++;
                    }

                    if (i + 1 < statements.size() && statements.get(i + 1) instanceof ElseNode) {
                        ifStatements.add(statements.get(i));
                        i++;
                    }

                    interpretIf(ifStatements);

                }

                if (statements.get(i) instanceof BreakNode) {
                    breakFlag = true;
                    break;
                }

                if (statements.get(i) instanceof ContinueNode) {
                    continue;
                }

                interpretStatement(statements.get(i));
            }

            if (breakFlag)
                break;

            result = evaluateCondition(condition);
        }

    }

    private void interpretFor(ForNode forStatement) {

        AssignmentNode initialization = forStatement.getInitialization();
        ExpressionNode condition = forStatement.getCondition();
        StatementNode update = forStatement.getUpdate();

        List<StatementNode> statements = forStatement.getStatements();

        interpretStatement(initialization);

        boolean result = evaluateCondition(condition);

        boolean breakFlag = false;

        while (result) {

            for (int i = 0; i < statements.size(); i++) {

                StatementNode statement = statements.get(i);

                if (statement instanceof IfNode) {
                    List<StatementNode> ifStatements = new ArrayList<>();

                    ifStatements.add(statement);

                    while (i + 1 < statements.size() && statements.get(i + 1) instanceof ElseIfNode) {
                        ifStatements.add(statements.get(i));
                        i++;
                    }

                    if (i + 1 < statements.size() && statements.get(i + 1) instanceof ElseNode) {
                        ifStatements.add(statements.get(i));
                        i++;
                    }

                    interpretIf(ifStatements);

                }

                if (statements.get(i) instanceof BreakNode) {
                    breakFlag = true;
                    break;
                }

                if (statements.get(i) instanceof ContinueNode) {
                    continue;
                }

                interpretStatement(statements.get(i));
            }

            if (breakFlag)
                break;

            interpretStatement(update);

            result = evaluateCondition(condition);
        }
    }

    private boolean evaluateCondition(ExpressionNode condition) {

        if (condition instanceof BinaryNode) {
            BinaryNode binaryNode = (BinaryNode) condition;

            switch (binaryNode.getOperator().getLexeme()) {

                case "==":
                    String left_value, right_value;

                    if (binaryNode.getLeft() instanceof VariableNode) {
                        left_value = symbolTable.lookup(((VariableNode) binaryNode.getLeft()).getName()).getValue();
                    } else if (binaryNode.getLeft() instanceof UnaryNode
                            || binaryNode.getLeft() instanceof LiteralNode) {
                        left_value = binaryNode.getLeft().toString();
                    } else {
                        left_value = String.valueOf(evaluateExpression(binaryNode.getLeft()));
                    }

                    if (binaryNode.getRight() instanceof VariableNode) {
                        right_value = symbolTable.lookup(((VariableNode) binaryNode.getRight()).getName()).getValue();
                    } else if (binaryNode.getRight() instanceof UnaryNode
                            || binaryNode.getRight() instanceof LiteralNode) {
                        right_value = binaryNode.getRight().toString();
                    } else {
                        right_value = String.valueOf(evaluateExpression(binaryNode.getRight()));
                    }

                    return right_value.equals(left_value);

                case ">":
                    return evaluateExpression(binaryNode.getLeft()) > evaluateExpression(binaryNode.getRight());
                case "<":
                    return evaluateExpression(binaryNode.getLeft()) < evaluateExpression(binaryNode.getRight());
                case "!=":
                    return evaluateExpression(binaryNode.getLeft()) != evaluateExpression(binaryNode.getRight());
                case ">=":
                    return evaluateExpression(binaryNode.getLeft()) >= evaluateExpression(binaryNode.getRight());
                case "<=":
                    return evaluateExpression(binaryNode.getLeft()) <= evaluateExpression(binaryNode.getRight());
                case "AND":
                    return evaluateCondition(binaryNode.getLeft()) && evaluateCondition(binaryNode.getRight());
                case "OR":
                    return evaluateCondition(binaryNode.getLeft()) || evaluateCondition(binaryNode.getRight());
                default:
                    error("Invalid operator", condition.getPosition());
            }

        } else if (condition instanceof UnaryNode) {
            UnaryNode unaryNode = (UnaryNode) condition;

            boolean operand = evaluateCondition(unaryNode.getOperand());

            switch (unaryNode.getOperator().getLexeme()) {
                case "NOT":
                    return !operand;
                default:
                    error("Invalid operator", condition.getPosition());
            }
        } else if (condition instanceof LiteralNode) {

            LiteralNode literalNode = (LiteralNode) condition;

            return literalNode.getValue().getLexeme().equals("\"TRUE\"");
        } else if (condition instanceof VariableNode) {
            VariableNode variableNode = (VariableNode) condition;

            Symbol symbol = symbolTable.lookup(variableNode.getName());

            return symbol.getValue().equals("\"TRUE\"");
        } else {
            error("Invalid condition", condition.getPosition());
        }

        return false;
    }

    private void error(String message, Position position) {
        System.err.println("Runtime Error: " + message + " " + position);
        System.exit(1);
    }
}