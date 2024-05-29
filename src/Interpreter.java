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
    private boolean displayError = true;

    public Interpreter(ProgramNode program, SymbolTable symbolTable) {
        this.program = program;
        this.symbolTable = symbolTable;
    }

    public void interpret() {

        for (Symbol s : symbolTable.getSymbols().values()) {
            System.out.println(s.getName() + " " + s.getType() + " " + s.getValue());
        }

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

        if (displayError) {
            System.out.println("No Error");
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

                UnaryNode unary = (UnaryNode) assignment.getExpression();

                if (unary.getOperand() instanceof LiteralNode) {
                    LiteralNode literal = (LiteralNode) unary.getOperand();

                    if (s.getType() != literal.getDataType()) {
                        error("Type mismatch. Assigning a " + literal.getDataType() + " to a " + s.getType(),
                                assignment.getVariable().getPosition());
                    }

                    if (unary.getOperator().getType() == Type.NOT) {

                        if (literal.toString().equals("TRUE")) {
                            s.setValue("FALSE");
                        } else {
                            s.setValue("TRUE");
                        }

                    } else if (unary.getOperator().getType() == Type.NEGATIVE) {
                        s.setValue("-" + unary.getOperand().toString());
                    } else {
                        s.setValue(unary.getOperand().toString());
                    }
                } else if (unary.getOperand() instanceof VariableNode) {
                    Symbol operand = symbolTable.lookup(((VariableNode) unary.getOperand()).getName());

                    if (s.getType() != operand.getType()) {
                        error("Type mismatch. Assigning a " + operand.getType() + " to a " + s.getType(),
                                assignment.getVariable().getPosition());
                    }

                    if (unary.getOperator().getType() == Type.NOT) {

                        if (operand.getValue().equals("TRUE")) {
                            s.setValue("FALSE");
                        } else {
                            s.setValue("TRUE");
                        }

                    } else if (unary.getOperator().getType() == Type.NEGATIVE) {

                        if (operand.getType() == Type.INT) {
                            int value = Integer.parseInt(operand.getValue()) * -1;
                            s.setValue(String.valueOf(value));

                        } else {
                            double value = Double.parseDouble(operand.getValue()) * -1;

                            s.setValue(String.valueOf(value));
                        }
                    }
                } else if (unary.getOperand() instanceof ExpressionNode) {

                    String result = evaluateExpression((ExpressionNode) unary.getOperand());

                    if (unary.getOperator().getType() == Type.NOT) {

                        if (result.equals("TRUE")) {
                            s.setValue("FALSE");
                        } else {
                            s.setValue("TRUE");
                        }

                    } else if (unary.getOperator().getType() == Type.NEGATIVE) {

                        if (result.contains(".")) {
                            double value = Double.parseDouble(result) * -1;
                            s.setValue(String.valueOf(value));
                        } else {
                            int value = Integer.parseInt(result) * -1;
                            s.setValue(String.valueOf(value));
                        }
                    }
                }

            } else if (assignment.getExpression() instanceof VariableNode) {

                Symbol left = symbolTable.lookup(assignment.getVariable().getName());
                Symbol right = symbolTable.lookup(((VariableNode) assignment.getExpression()).getName());

                left.setValue(right.getValue());
            } else {

                String result = evaluateExpression((ExpressionNode) assignment.getExpression());

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
            displayError = false;
            interpretDisplay((DisplayNode) statement);
        } else if (statement instanceof ScanNode) {
            displayError = false;
            interpretScan((ScanNode) statement);
        } else if (statement instanceof WhileNode) {
            interpretWhile((WhileNode) statement);
        } else if (statement instanceof ForNode) {
            interpretFor((ForNode) statement);
        }
    }

    public String evaluateExpression(ExpressionNode expression) {
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
            } else if (token.getType() == Type.NEGATIVE || token.getType() == Type.POSITIVE
                    || token.getType() == Type.NOT) {
                operatorStack.push(token);
            } else if (token.getLexeme().equals("(")) {
                operatorStack.push(token);
            } else if (token.getLexeme().equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().getLexeme().equals("(")) {
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

    private boolean hasHigherPrecedence(Token a, Token b) {
        int precedence1 = getOperatorPrecedence(a);
        int precedence2 = getOperatorPrecedence(b);
        return precedence1 >= precedence2;
    }

    private int getOperatorPrecedence(Token token) {
        switch (token.getType()) {
            case ADD:
            case SUBTRACT:
                return 1;
            case MULTIPLY:
            case DIVIDE:
            case MODULO:
                return 2;
            case LESS:
            case GREATER:
            case LESS_EQUAL:
            case GREATER_EQUAL:
            case NOT_EQUAL:
            case EQUAL:
                return 3;
            case AND:
                return 4;
            case OR:
                return 5;
            case NOT:
            case POSITIVE:
            case NEGATIVE:
                return 6;
            default:
                return 0;
        }
    }

    public String evaluatePostfix(List<Token> postfixExpression) {
        Stack<Object> stack = new Stack<>();

        for (int i = 0; i < postfixExpression.size(); i++) {
            Token token = postfixExpression.get(i);
            String lexeme = token.getLexeme();

            if (token.getType() == Type.LITERAL) {
                if (token.getLexeme().contains(".")) {
                    stack.push(Double.valueOf(lexeme));
                } else if (token.getLexeme().equals("TRUE") || token.getLexeme().equals("FALSE")) {
                    stack.push(Boolean.valueOf(lexeme));
                } else {
                    try {
                        stack.push(Integer.parseInt(lexeme));
                    } catch (NumberFormatException e) {
                        stack.push(lexeme.charAt(0));
                    }
                }

            } else if (token.getType() == Type.IDENTIFIER) {
                if (symbolTable.lookup(lexeme) != null) {

                    if (symbolTable.lookup(lexeme).getType() == Type.INT) {
                        stack.push(Integer.parseInt(symbolTable.lookup(lexeme).getValue()));
                    } else if (symbolTable.lookup(lexeme).getType() == Type.FLOAT) {
                        stack.push(Double.parseDouble(symbolTable.lookup(lexeme).getValue()));
                    } else if (symbolTable.lookup(lexeme).getType() == Type.BOOL) {
                        stack.push(Boolean.parseBoolean(symbolTable.lookup(lexeme).getValue()));
                    } else {
                        stack.push(symbolTable.lookup(lexeme).getValue());
                    }
                } else {
                    error("Undefined variable: " + lexeme, token.getPosition());
                }
            } else {
                switch (token.getType()) {
                    case ADD:
                    case SUBTRACT:
                    case MULTIPLY:
                    case DIVIDE:
                    case MODULO:
                        Number right = getNumber(stack.pop());
                        Number left = getNumber(stack.pop());
                        Number result = calculate(left, right, token);
                        stack.push(result);
                        break;
                    case LESS:
                        right = getNumber(stack.pop());
                        left = getNumber(stack.pop());

                        if (left instanceof Integer && right instanceof Integer) {
                            stack.push((left.intValue() < right.intValue()));
                        } else {
                            stack.push((left.doubleValue() < right.doubleValue()));
                        }
                        break;
                    case GREATER:
                        right = getNumber(stack.pop());
                        left = getNumber(stack.pop());

                        if (left instanceof Integer && right instanceof Integer) {
                            stack.push((left.intValue() > right.intValue()));
                        } else {
                            stack.push((left.doubleValue() > right.doubleValue()));
                        }

                        break;
                    case LESS_EQUAL:
                        right = getNumber(stack.pop());
                        left = getNumber(stack.pop());

                        if (left instanceof Integer && right instanceof Integer) {
                            stack.push((left.intValue() <= right.intValue()));
                        } else {
                            stack.push((left.doubleValue() <= right.doubleValue()));
                        }
                        break;
                    case GREATER_EQUAL:
                        right = getNumber(stack.pop());
                        left = getNumber(stack.pop());

                        if (left instanceof Integer && right instanceof Integer) {
                            stack.push((left.intValue() >= right.intValue()));
                        } else {
                            stack.push((left.doubleValue() >= right.doubleValue()));
                        }
                        break;
                    case NOT_EQUAL:
                        Object rightNotEqual = stack.pop();
                        Object leftNotEqual = stack.pop();
                        if (leftNotEqual instanceof Number && rightNotEqual instanceof Number) {
                            stack.push(!leftNotEqual.equals(rightNotEqual));
                        } else {
                            stack.push(!leftNotEqual.toString().equals(rightNotEqual.toString()));
                        }
                        break;
                    case EQUAL:
                        Object rightEqual = stack.pop();
                        Object leftEqual = stack.pop();
                        if (leftEqual instanceof Number && rightEqual instanceof Number) {
                            stack.push(leftEqual.equals(rightEqual));
                        } else {
                            stack.push(leftEqual.toString().equals(rightEqual.toString()));
                        }
                        break;
                    case AND:
                        stack.push(((boolean) stack.pop() && (boolean) stack.pop()));
                        break;
                    case OR:
                        stack.push(((boolean) stack.pop() || (boolean) stack.pop()));
                        break;
                    case NOT:
                        stack.push(!(boolean) stack.pop());
                        break;
                    case POSITIVE:
                        stack.push(stack.pop());
                        break;
                    case NEGATIVE:
                        stack.push(-(double) stack.pop());
                        break;
                    default:
                        error("Unknown operator: " + lexeme, token.getPosition());
                        break;
                }
            }
        }

        Object result = stack.pop();
        if (result instanceof Boolean) {
            return (boolean) result ? "TRUE" : "FALSE";
        } else {
            return result.toString();
        }
    }

    private void interpretDisplay(DisplayNode display) {
        List<Token> arguments = display.getArguments();
        List<ExpressionNode> expressions = display.getExpressions();

        int currentIndexExpression = 0;

        for (Token token : arguments) {

            if (token.getType() == Type.STRING_LITERAL || token.getType() == Type.SPECIAL_CHARACTER) {
                System.out.print(token.getLexeme());
                continue;
            }

            if (token.getType() == Type.IDENTIFIER) {
                Symbol symbol = symbolTable.lookup(token.getLexeme());

                String value = symbol.getValue();

                System.out.print(value);
                continue;
            }

            if (token.getType() == Type.LITERAL) {
                System.out.print(token.getLexeme());
                continue;
            }

            if (token.getType() == Type.EXPRESSION) {
                ExpressionNode expression = expressions.get(currentIndexExpression);
                String result = evaluateExpression(expression);

                if (result.equals("\"TRUE\"")) {
                    result = "TRUE";
                } else if (result.equals("\"FALSE\"")) {
                    result = "FALSE";
                }

                System.out.print(result);

                currentIndexExpression++;
                continue;
            }

            if (token.getType() == Type.NEXT_LINE) {
                System.out.println();
                continue;
            }

            if (token.getType() == Type.CONCATENATION) {
                continue;
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

            String conditionResult = evaluateExpression(condition);

            if (conditionResult.equals("\"TRUE\"")) {
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

            String conditionResult = evaluateExpression(condition);

            if (conditionResult.equals("\"TRUE\"")) {

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

        String result = evaluateExpression(condition);

        boolean breakFlag = false;

        while (result.equals("TRUE")) {

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
                    break;
                }

                interpretStatement(statements.get(i));
            }

            if (breakFlag)
                break;

            result = evaluateExpression(condition);
        }

    }

    private void interpretFor(ForNode forStatement) {

        AssignmentNode initialization = forStatement.getInitialization();
        ExpressionNode condition = forStatement.getCondition();
        StatementNode update = forStatement.getUpdate();

        List<StatementNode> statements = forStatement.getStatements();

        interpretStatement(initialization);

        String result = evaluateExpression(condition);

        boolean breakFlag = false;

        while (result == "\"TRUE\"") {

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

            result = evaluateExpression(condition);
        }
    }

    private Number calculate(Number left, Number right, Token token) {
        if (left instanceof Double || right instanceof Double) {
            switch (token.getType()) {
                case Type.ADD:
                    return left.doubleValue() + right.doubleValue();
                case Type.SUBTRACT:
                    return left.doubleValue() - right.doubleValue();
                case Type.MULTIPLY:
                    return left.doubleValue() * right.doubleValue();
                case Type.DIVIDE:
                    if (right.doubleValue() == 0) {
                        error("Cannot divide by zero", token.getPosition());
                    }
                    return left.doubleValue() / right.doubleValue();
                default:
                    error("Unknown operator: " + token.getLexeme(), token.getPosition());
            }
        } else {
            switch (token.getType()) {
                case Type.ADD:
                    return left.intValue() + right.intValue();
                case Type.SUBTRACT:
                    return left.intValue() - right.intValue();
                case Type.MULTIPLY:
                    return left.intValue() * right.intValue();
                case Type.DIVIDE:
                    if (right.intValue() == 0) {
                        error("Cannot divide by zero", token.getPosition());
                    }
                    return left.intValue() / right.intValue();
                default:
                    error("Unknown operator: " + token.getLexeme(), token.getPosition());
            }
        }
        return 0;
    }

    private Number getNumber(Object number) {

        if (number instanceof Integer) {
            return (Integer) number;
        } else if (number instanceof Double) {
            return (Double) number;
        } else {
            return null;
        }
    }

    private void error(String message, Position position) {
        // System.err.println("Runtime Error: " + message + " " + position);
        // System.exit(1);

        throw new RuntimeException("Runtime Error: " + message + " " + position);
    }
}