package src.analyzer;

import src.nodes.*;
import src.utils.EvaluationResult;
import src.utils.Position;
import src.utils.Symbol;
import src.utils.SymbolTable;
import src.utils.Token;
import src.utils.Type;

import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticAnalyzer {
    private SymbolTable symbolTable;
    private SymbolTable initialSymbolTable;
    private ProgramNode programNode;

    public SemanticAnalyzer(ProgramNode programNode) {
        this.programNode = programNode;
        symbolTable = new SymbolTable();
    }

    // Analyze the AST
    public void analyze() {

        for (VariableDeclarationNode declaration : programNode.getDeclarations()) {

            Symbol symbol = new Symbol(declaration.getType(), declaration.getName(), declaration.getValue());
            ;

            checkUsingReservedKeyword(declaration.getName(), declaration.getPosition());
            checkValidVariableName(declaration.getName(), declaration.getPosition());

            if (declaration.getValue() != null) {
                checkValidDataType(declaration.getType(), declaration.getValue(), declaration.getPosition());
            }

            if (!symbolTable.insert(symbol)) {
                error("Variable '" + declaration.getName() + "' was already declared", declaration.getPosition());
            }
        }

        initialSymbolTable = symbolTable.copy();

        for (StatementNode statement : programNode.getStatements()) {
            visit(statement);
        }
    }

    // Visit an AST node
    private void visit(StatementNode node) {
        if (node instanceof AssignmentNode) {
            visitAssignmentNode((AssignmentNode) node);
        } else if (node instanceof DisplayNode) {
            visitDisplayNode((DisplayNode) node);
        } else if (node instanceof ScanNode) {
            visitScanNode((ScanNode) node);
        } else if (node instanceof IfNode || node instanceof ElseNode || node instanceof ElseIfNode) {
            visitIfNode(node);
        } else if (node instanceof WhileNode) {
            visitWhileNode((WhileNode) node);
        } else if (node instanceof ForNode) {
            visitForNode((ForNode) node);
        }
    }

    // Visit an assignment node
    private void visitAssignmentNode(AssignmentNode node) {

        VariableNode variableNode = node.getVariable();
        ExpressionNode expressionNode = node.getExpression();

        // We pass the type of the variable to the visitVariableNode method since we
        // don't need to check the type of the variable
        visitVariableNode(variableNode, null, false);

        Symbol leftSymbol = symbolTable.lookup(node.getVariable().getName());

        if (expressionNode instanceof VariableNode) {

            visitVariableNode((VariableNode) expressionNode, leftSymbol.getType(), true);

            Symbol rightSymbol = symbolTable.lookup(((VariableNode) expressionNode).getName());

            leftSymbol.setValue(rightSymbol.getValue());

        } else if (expressionNode instanceof LiteralNode) {

            if (leftSymbol.getType() != ((LiteralNode) expressionNode).getDataType()) {

                error("Invalid type in assignment. Left is " + leftSymbol.getType() + " and right is "
                        + ((LiteralNode) expressionNode).getDataType(), expressionNode.getPosition());
            }

            leftSymbol.setValue(((LiteralNode) expressionNode).getValue().getLexeme());
        } else if (expressionNode instanceof UnaryNode) {

            UnaryNode unaryNode = (UnaryNode) expressionNode;

            if (unaryNode.getOperator().getType() == Type.NOT) {

                if (leftSymbol.getType() != Type.BOOL) {
                    error("Invalid assignment. Left Symbol is an " + leftSymbol.getType() + " datatype.",
                            expressionNode.getPosition());
                }

                ExpressionNode operand = unaryNode.getOperand();

                if (operand instanceof VariableNode) {
                    visitVariableNode((VariableNode) operand, leftSymbol.getType(), true);

                    Symbol rightSymbol = symbolTable.lookup(((VariableNode) operand).getName());

                    if (rightSymbol.getType() != Type.BOOL) {
                        error("Invalid NOT operation. Expected BOOL but got" + rightSymbol.getType(),
                                expressionNode.getPosition());
                    }

                    if (rightSymbol.getValue().equals("TRUE")) {
                        leftSymbol.setValue("FALSE");
                    } else {
                        leftSymbol.setValue("TRUE");
                    }
                } else if (operand instanceof LiteralNode) {

                    if (leftSymbol.getType() != ((LiteralNode) operand).getDataType()) {
                        error("Invalid type in assignment. Left is " + leftSymbol.getType() + " and right is "
                                + ((LiteralNode) operand).getDataType(), expressionNode.getPosition());
                    }

                    boolean result = !Boolean.parseBoolean(((LiteralNode) operand).getValue().getLexeme());

                    leftSymbol.setValue(String.valueOf(result).toUpperCase());
                }

            } else {

                ExpressionNode operand = unaryNode.getOperand();

                if (operand instanceof VariableNode) {
                    visitVariableNode((VariableNode) operand, leftSymbol.getType(), true);

                    Symbol rightSymbol = symbolTable.lookup(((VariableNode) operand).getName());

                    if (unaryNode.getOperator().getType() == Type.NEGATIVE) {
                        if (rightSymbol.getType() == Type.INT) {
                            int value = Integer.parseInt(rightSymbol.getValue()) * -1;
                            leftSymbol.setValue(String.valueOf(value));

                        } else if (rightSymbol.getType() == Type.FLOAT) {
                            double value = Double.parseDouble(rightSymbol.getValue()) * -1;

                            leftSymbol.setValue(String.valueOf(value));
                        } else {
                            error("Invalid operation. Cannot negate a " + rightSymbol.getType(),
                                    expressionNode.getPosition());
                        }
                    } else {
                        if (rightSymbol.getType() != Type.INT && rightSymbol.getType() != Type.FLOAT) {
                            error("Invalid operation. Expected an INT or FLOAT after a unary operator "
                                    + rightSymbol.getType() + " datatype.",
                                    operand.getPosition());
                        }
                    }

                    leftSymbol.setValue(rightSymbol.getValue());

                } else if (operand instanceof LiteralNode) {

                    LiteralNode rightLiteral = (LiteralNode) operand;

                    if (leftSymbol.getType() != rightLiteral.getDataType()) {
                        error("Invalid type in assignment. Left is " + leftSymbol.getType() + " and right is "
                                + ((LiteralNode) operand).getDataType(), expressionNode.getPosition());
                    }

                    if (unaryNode.getOperator().getType() == Type.NEGATIVE) {
                        if (rightLiteral.getDataType() == Type.INT) {
                            int value = Integer.parseInt(rightLiteral.toString()) * -1;
                            leftSymbol.setValue(String.valueOf(value));

                        } else if (rightLiteral.getDataType() == Type.FLOAT) {
                            double value = Double.parseDouble(rightLiteral.toString()) * -1;

                            leftSymbol.setValue(String.valueOf(value));
                        } else {
                            error("Invalid operation. Cannot negate a " + rightLiteral.getDataType(),
                                    expressionNode.getPosition());
                        }
                    } else {
                        if (rightLiteral.getDataType() != Type.INT && rightLiteral.getDataType() != Type.FLOAT) {

                            error("Invalid operation. Cannot negate a " + rightLiteral.getDataType() + " datatype.",
                                    rightLiteral.getPosition());
                        }
                    }

                    leftSymbol.setValue(((LiteralNode) operand).getValue().getLexeme());
                }

            }
        } else {

            EvaluationResult result = evaluateExpression(expressionNode);

            if (leftSymbol.getType() != result.getType()) {
                error("Invalid type in assignment. Left is " + leftSymbol.getType() + " and right is "
                        + result.getType(),
                        expressionNode.getPosition());
            }

            leftSymbol.setValue(result.getValue());
        }
    }

    // Visit a variable node
    private void visitVariableNode(VariableNode node, Type type, boolean checkInitialized) {

        String name = node.getName();

        checkUsingReservedKeyword(name, node.getPosition());

        checkValidVariableName(name, node.getPosition());

        Symbol symbol = symbolTable.lookup(name);

        if (symbol == null) {
            error("Variable '" + name + "' is not declared", node.getPosition());
        }

        if (checkInitialized && !symbol.isInitialized()) {
            error("Variable '" + name + "' is not initialized", node.getPosition());
        }

        if (type == null) {
            return;
        }

        if (symbol.getType() != type) {
            error("Variable '" + name + "' is not of type " + type, node.getPosition());
        }
    }

    // Visit a display node
    private void visitDisplayNode(DisplayNode node) {
        List<Token> arguments = node.getArguments();
        List<ExpressionNode> expressions = node.getExpressions();
        int currentIndexExpression = 0;

        for (Token argument : arguments) {

            if (argument.getType() == Type.IDENTIFIER) {
                visitVariableNode(new VariableNode(argument), null, true);
            } else if (argument.getType() == Type.LITERAL) {
                LiteralNode literalNode = new LiteralNode(argument);
                checkValidDataType(literalNode.getDataType(), argument.getLexeme(), argument.getPosition());
            } else if (argument.getType() == Type.EXPRESSION) {
                ExpressionNode expression = expressions.get(currentIndexExpression);
                evaluateExpression(expression);
                currentIndexExpression++;
            }
        }
    }

    // Visit a scan node
    private void visitScanNode(ScanNode node) {

        for (Token identifier : node.getIdentifiers()) {

            Symbol symbol = symbolTable.lookup(identifier.getLexeme());

            if (symbol == null) {
                error("Variable '" + identifier + "' is not declared", node.getPosition());
            }

            symbol.setInitialized(true);
        }
    }

    // Visit an if node
    private void visitIfNode(StatementNode node) {

        if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;

            if (evaluateExpression(ifNode.getCondition()).getType() != Type.BOOL) {
                error("Expected a BOOL expression in if condition",
                        ifNode.getCondition().getPosition());
            }

            for (StatementNode statement : ifNode.getStatements()) {
                visit(statement);
            }

        } else if (node instanceof ElseNode) {
            ElseNode elseNode = (ElseNode) node;

            for (StatementNode statement : elseNode.getStatements()) {
                visit(statement);
            }

        } else if (node instanceof ElseIfNode) {
            ElseIfNode elseIfNode = (ElseIfNode) node;

            if (evaluateExpression(elseIfNode.getCondition()).getType() != Type.BOOL) {
                error("Invalid type in condition. Expected BOOL but got "
                        + evaluateExpression(elseIfNode.getCondition()),
                        elseIfNode.getCondition().getPosition());
            }

            for (StatementNode statement : elseIfNode.getStatements()) {
                visit(statement);
            }
        }
    }

    private EvaluationResult evaluateExpression(ExpressionNode condition) {

        if (condition instanceof BinaryNode) {
            BinaryNode binaryNode = (BinaryNode) condition;

            EvaluationResult leftResult = evaluateExpression(binaryNode.getLeft());
            EvaluationResult rightResult = evaluateExpression(binaryNode.getRight());

            Type operatorType = binaryNode.getOperator().getType();

            if (leftResult.getType() != rightResult.getType()) {
                error("Invalid types in condition. Left is " + leftResult.getType() + " and right is "
                        + rightResult.getType(),
                        condition.getPosition());
            }

            String result = null;

            if (operatorType == Type.AND || operatorType == Type.OR) {
                if (leftResult.getType() != Type.BOOL && rightResult.getType() != Type.BOOL) {
                    error("Invalid operation. Both operands must be BOOL", null);
                    return null;
                }

                boolean boolResult;
                if (operatorType == Type.AND) {
                    boolResult = Boolean.parseBoolean(leftResult.getValue())
                            && Boolean.parseBoolean(rightResult.getValue());
                } else { // operatorType == Type.OR
                    boolResult = Boolean.parseBoolean(leftResult.getValue())
                            || Boolean.parseBoolean(rightResult.getValue());
                }

                result = boolResult ? "TRUE" : "FALSE";

                return new EvaluationResult(Type.BOOL, result);

            } else if (operatorType == Type.EQUAL || operatorType == Type.NOT_EQUAL) {

                boolean boolResult;
                if (operatorType == Type.EQUAL) {

                    if (leftResult.getValue() != null && rightResult.getValue() != null) {
                        boolResult = leftResult.getValue().equals(rightResult.getValue());
                    } else {
                        boolResult = false;
                    }
                } else { // operatorType == Type.NOT_EQUAL
                    if (leftResult.getValue() != null && rightResult.getValue() != null) {
                        boolResult = leftResult.getValue().equals(rightResult.getValue());
                    } else {
                        boolResult = false;
                    }
                }

                result = boolResult ? "TRUE" : "FALSE";

                return new EvaluationResult(Type.BOOL, result);

            } else if (operatorType == Type.GREATER || operatorType == Type.LESS || operatorType == Type.GREATER_EQUAL
                    || operatorType == Type.LESS_EQUAL) {

                if (leftResult.getType() != Type.INT && leftResult.getType() != Type.FLOAT) {
                    error("Invalid operation. Left operand must be a number", null);
                }

                if (rightResult.getType() != Type.INT && rightResult.getType() != Type.FLOAT) {
                    error("Invalid operation. Right operand must be a number", null);
                }

                double left = Double.parseDouble(leftResult.getValue());
                double right = Double.parseDouble(leftResult.getValue());

                boolean boolResult;
                if (operatorType == Type.GREATER) {
                    boolResult = left > right;
                } else if (operatorType == Type.LESS) {
                    boolResult = left < right;
                } else if (operatorType == Type.GREATER_EQUAL) {
                    boolResult = left >= right;
                } else { // operatorType == Type.LESS_EQUAL
                    boolResult = left <= right;
                }

                result = boolResult ? "TRUE" : "FALSE";

                return new EvaluationResult(Type.BOOL, result);

            } else if (operatorType == Type.ADD || operatorType == Type.SUBTRACT || operatorType == Type.MULTIPLY
                    || operatorType == Type.DIVIDE || operatorType == Type.MODULO) {

                if (leftResult.getType() != Type.INT && leftResult.getType() != Type.FLOAT) {
                    error("Invalid operation. Left operand must be a number", null);
                }

                if (rightResult.getType() != Type.INT && rightResult.getType() != Type.FLOAT) {
                    error("Invalid operation. Right operand must be a number", null);
                }

                double left = Double.parseDouble(leftResult.getValue());
                double right = Double.parseDouble(rightResult.getValue());

                double operationResult;
                if (operatorType == Type.ADD) {
                    operationResult = left + right;
                } else if (operatorType == Type.SUBTRACT) {
                    operationResult = left - right;
                } else if (operatorType == Type.MULTIPLY) {
                    operationResult = left * right;
                } else if (operatorType == Type.DIVIDE) {
                    operationResult = left / right;
                } else { // operatorType == Type.MODULO
                    operationResult = left % right;
                }

                result = String.valueOf(operationResult);

                if (leftResult.getType() == Type.INT && rightResult.getType() == Type.INT) {
                    return new EvaluationResult(Type.INT, result);
                } else {
                    return new EvaluationResult(Type.FLOAT, result);
                }
            }

        } else if (condition instanceof VariableNode) {
            VariableNode variableNode = (VariableNode) condition;

            Symbol symbol = symbolTable.lookup(variableNode.getName());

            visitVariableNode(variableNode, symbol.getType(), true);

            return new EvaluationResult(symbol.getType(), symbol.getValue());

        } else if (condition instanceof LiteralNode) {

            LiteralNode literalNode = (LiteralNode) condition;

            checkValidDataType(literalNode.getDataType(), literalNode.getValue().getLexeme(),
                    literalNode.getPosition());

            return new EvaluationResult(literalNode.getDataType(), literalNode.getValue().getLexeme());

        } else if (condition instanceof UnaryNode) {
            UnaryNode unaryNode = (UnaryNode) condition;
            EvaluationResult operandResult = evaluateExpression(unaryNode.getOperand());

            if (unaryNode.getOperator().getType() == Type.NOT) {
                if (operandResult.getType() == Type.BOOL) {

                    Boolean resultValue = Boolean.parseBoolean(operandResult.getValue());

                    String result = resultValue ? "FALSE" : "TRUE";

                    return new EvaluationResult(Type.BOOL, result);
                } else {
                    error("Invalid operand type for 'NOT': expected 'BOOL', but got '" + operandResult.getType()
                            + "'", condition.getPosition());
                }
            } else if (unaryNode.getOperator().getType() == Type.NEGATIVE) {
                if (operandResult.getType() == Type.INT) {

                    int resultValue = Integer.parseInt(operandResult.getValue()) * -1;

                    return new EvaluationResult(Type.INT, String.valueOf(resultValue));

                } else if (operandResult.getType() == Type.FLOAT) {

                    double resultValue = Double.parseDouble(operandResult.getValue()) * -1;

                    return new EvaluationResult(Type.FLOAT, String.valueOf(resultValue));
                } else {
                    error("Invalid operand type for NEGATIVE '-'. Expected INT or FLOAT but got "
                            + operandResult.getType(),
                            condition.getPosition());
                }
            } else if (unaryNode.getOperator().getType() == Type.POSITIVE) {
                if (operandResult.getType() == Type.INT) {

                    int resultValue = Integer.parseInt(operandResult.getValue());

                    return new EvaluationResult(Type.INT, String.valueOf(resultValue));

                } else if (operandResult.getType() == Type.FLOAT) {

                    double resultValue = Double.parseDouble(operandResult.getValue());

                    return new EvaluationResult(Type.FLOAT, String.valueOf(resultValue));
                } else {
                    error("Invalid operand type for POSITIVE '+'. Expected INT or FLOAT but got "
                            + operandResult.getType(),
                            condition.getPosition());
                }
            } else {
                error("Invalid unary operator", condition.getPosition());
            }
        } else {
            error("Invalid condition", condition.getPosition());
        }

        return null;
    }

    // Visit a while node
    private void visitWhileNode(WhileNode node) {
        if (evaluateExpression(node.getCondition()).getType() != Type.BOOL) {
            error("Invalid type in condition. Expected BOOL but got " + evaluateExpression(node.getCondition()),
                    node.getCondition().getPosition());
        }

        for (StatementNode statement : node.getStatements()) {
            visit(statement);
        }
    }

    // Visit a for node
    private void visitForNode(ForNode node) {

        if (node.getInitialization() != null) {
            visitAssignmentNode(node.getInitialization());
        }

        if (evaluateExpression(node.getCondition()).getType() != Type.BOOL) {
            error("Invalid type in condition. Expected BOOL but got " + evaluateExpression(node.getCondition()),
                    node.getCondition().getPosition());
        }

        visitAssignmentNode((AssignmentNode) node.getUpdate());

        for (StatementNode statement : node.getStatements()) {
            visit(statement);
        }
    }

    public SymbolTable getInitialSymbolTable() {
        return initialSymbolTable;
    }

    private void checkValidVariableName(String name, Position position) {
        String regex = "^[a-zA-Z_][a-zA-Z0-9_]*$";

        // Compile the regular expression
        Pattern pattern = Pattern.compile(regex);

        // Create a matcher to match the input string with the pattern
        Matcher matcher = pattern.matcher(name);

        // Return true if the input string matches the pattern, otherwise false
        if (!matcher.matches()) {
            error("Variable '" + name + "' is not a valid variable name", position);
        }
    }

    private void checkUsingReservedKeyword(String name, Position position) {

        Set<String> reservedKeywords = new HashSet<>(
                Arrays.asList("IF", "ELSE", "FOR", "WHILE", "BEGIN", "END", "DISPLAY", "SCAN", "INT", "CHAR", "BOOL",
                        "FLOAT", "AND", "OR", "NOT", "TRUE", "FALSE"));

        if (reservedKeywords.contains(name)) {
            error("Variable name: '" + name + "' can't be used because it is a reserved keyword", position);
        }
    }

    private void checkValidDataType(Type type, String value, Position position) {

        if (type == Type.INT) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                error("Invalid value for INT datatype", position);
            }
        } else if (type == Type.FLOAT) {
            try {
                Float.parseFloat(value);
            } catch (NumberFormatException e) {
                error("Invalid value for FLOAT datatype", position);
            }
        } else if (type == Type.BOOL) {
            if (!value.equals("TRUE") && !value.equals("FALSE")) {
                error("Invalid value for BOOL datatype", position);
            }
        }
    }

    // Report an error
    private void error(String message, Position position) {
        System.err.println("Semantics Error: " + message + " at Line " +
                position.getLine() + " and Column " + position.getColumn());
        System.exit(1);

        // for debugging purposes so we know where the error is
        // Remove when checking
        // throw new RuntimeException("Semantics error " + ": " + message + " at Line "
        // + position.getLine()
        // + " and Column " + position.getColumn());
    }
}