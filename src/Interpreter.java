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
        int displayCounter = 0;

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

                // Display Counter
                if (statement instanceof DisplayNode) {
                    displayCounter++;
                }

                interpretStatement(statement);
            }
        }

        if (displayCounter == 0) {
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

                if (s.getType() != Type.FLOAT || s.getType() != Type.INT) {
                    error("Type mismatch. Assigning a Number to a ", assignment.getVariable().getPosition());
                }

                s.setValue(assignment.getExpression().toString());

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
            interpretDisplay((DisplayNode) statement);
        } else if (statement instanceof ScanNode) {
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
                stack.push(Double.valueOf(lexeme));
            } else if (token.getType() == Type.IDENTIFIER) {
                if (symbolTable.lookup(lexeme) != null) {
                    stack.push(Double.parseDouble(symbolTable.lookup(lexeme).getValue()));
                } else {
                    error("Undefined variable: " + lexeme, token.getPosition());
                }
            } else {
                switch (token.getType()) {
                    case ADD:
                        stack.push((double) stack.pop() + (double) stack.pop());
                        break;
                    case SUBTRACT:
                        double subtractor = (double) stack.pop();
                        stack.push((double) stack.pop() - subtractor);
                        break;
                    case MULTIPLY:
                        stack.push((double) stack.pop() * (double) stack.pop());
                        break;
                    case DIVIDE:
                        double divisor = (double) stack.pop();
                        if (divisor != 0.0) {
                            stack.push((double) stack.pop() / divisor);
                        } else {
                            error("Cannot divide by zero", token.getPosition());
                        }
                        break;
                    case MODULO:
                        stack.push((double) stack.pop() % (double) stack.pop());
                        break;
                    case LESS:
                        double rightLess = (double) stack.pop();
                        stack.push((double) stack.pop() < rightLess);
                        break;
                    case GREATER:
                        double rightGreater = (double) stack.pop();
                        stack.push((double) stack.pop() > rightGreater);
                        break;
                    case LESS_EQUAL:
                        double rightLessEqual = (double) stack.pop();
                        stack.push((double) stack.pop() <= rightLessEqual);
                        break;
                    case GREATER_EQUAL:
                        double rightGreaterEqual = (double) stack.pop();
                        stack.push((double) stack.pop() >= rightGreaterEqual);
                        break;
                    case NOT_EQUAL:
                        double rightNotEqual = (double) stack.pop();
                        stack.push(!stack.pop().equals(rightNotEqual));
                        break;
                    case EQUAL:
                        double rightEqual = (double) stack.pop();
                        stack.push(stack.pop().equals(rightEqual));
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
            return (boolean) result ? "\"TRUE\"" : "\"FALSE\"";
        } else {
            return result.toString();
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

        while (result.equals("\"TRUE\"")) {

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

    private void error(String message, Position position) {
        System.err.println("Runtime Error: " + message + " " + position);
        System.exit(1);
    }
}