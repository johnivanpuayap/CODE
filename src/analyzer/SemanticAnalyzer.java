package src.analyzer;

import src.nodes.*;
import src.utils.Position;
import src.utils.Symbol;
import src.utils.SymbolTable;
import src.utils.Token;
import src.utils.Type;

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

            checkValidVariableName(declaration.getName(), declaration.getPosition());

            if (!symbolTable.insert(symbol)) {
                error("Variable '" + declaration.getName() + "' is already declared", declaration.getPosition());
            }
        }

        initialSymbolTable = symbolTable;

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
        } else if (node instanceof IfNode) {
            visitIfNode((IfNode) node);
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

        for (Token argument : arguments) {

            if (argument.getType() == Type.IDENTIFIER) {

                visitVariableNode(new VariableNode(argument), null, true);
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
        }
    }

    // Visit an if node
    private void visitIfNode(IfNode node) {
        evaluateCondition((ExpressionNode) node.getCondition());

        for (StatementNode statement : node.getStatements()) {
            visit(statement);
        }
    }

    private Type evaluateCondition(ExpressionNode condition) {

        if (condition instanceof BinaryNode) {
            BinaryNode binaryNode = (BinaryNode) condition;

            Type left = evaluateCondition(binaryNode.getLeft());
            Type right = evaluateCondition(binaryNode.getRight());

            if (binaryNode.getOperator().getType() == Type.EQUAL
                    || binaryNode.getOperator().getType() == Type.NOT_EQUAL) {

                if (left != right) {

                    error("Invalid types in condition. Left is " + left + " and right is " + right,
                            condition.getPosition());
                }
            } else {
                if (left != Type.INT || right != Type.INT) {
                    error("Invalid types in condition", condition.getPosition());
                }
            }

        } else if (condition instanceof VariableNode) {

            VariableNode variableNode = (VariableNode) condition;

            System.out.println("Variable: " + variableNode.getName());

            // We pass the type of the variable to the visitVariableNode method since we
            // don't need to check the type of the variable
            visitVariableNode(variableNode, null, true);

            Symbol symbol = symbolTable.lookup(variableNode.getName());

            return symbol.getType();

        } else if (condition instanceof LiteralNode) {

            System.out.println("Literal: " + ((LiteralNode) condition).getValue().getLexeme());

            return ((LiteralNode) condition).getDataType();
        } else {
            error("Invalid condition", condition.getPosition());
        }

        return null;
    }

    // Visit a while node
    private void visitWhileNode(WhileNode node) {
        evaluateCondition(node.getCondition());

        for (StatementNode statement : node.getStatements()) {
            visit(statement);
        }
    }

    // Visit a for node
    private void visitForNode(ForNode node) {
        visitAssignmentNode(node.getInitialization());
        evaluateCondition(node.getCondition());
        visitAssignmentNode((AssignmentNode) node.getUpdate());

        for (StatementNode statement : node.getStatements()) {
            visit(statement);
        }
    }

    // Report an error
    private void error(String message, Position position) {
        // System.err.println("Error at " + position + ": " + message);
        // System.exit(1);

        // for debugging purposes so we know where the error is
        // Remove when checking
        throw new RuntimeException("Error at " + position + ": " + message);
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
}