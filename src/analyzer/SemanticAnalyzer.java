package src.analyzer;

import src.nodes.*;
import src.utils.Position;
import src.utils.Symbol;
import src.utils.SymbolTable;
import src.utils.Token;

import java.util.List;

// Check if the variable was declared and initialized before using it

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
            if (!symbolTable.insert(symbol)) {
                error("Variable '" + declaration.getName() + "' is already declared", declaration.getPosition());
            }
        }

        initialSymbolTable = symbolTable;

        for (StatementNode statement : programNode.getStatements()) {
            try {
                visit(statement);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
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
        }
    }

    // Visit an assignment node
    private void visitAssignmentNode(AssignmentNode node) {

        Symbol symbol = symbolTable.lookup(node.getVariable().getName());

        visitVariableNode(node.getVariable());

        symbol.setValue(evaluate(node.getExpression()));
    }

    private String evaluate(ExpressionNode node) {
        return null;
    }

    // Visit a variable node
    private void visitVariableNode(VariableNode node) {

        String name = node.getName();

        Symbol symbol = symbolTable.lookup(name);
        if (symbol == null) {
            error("Variable '" + name + "' is not declared", node.getPosition());
        }
        if (!symbol.isInitialized()) {
            error("Variable '" + name + "' is not initialized", node.getPosition());
        }
    }

    // Visit a display node
    private void visitDisplayNode(DisplayNode node) {
        List<Token> arguments = node.getArguments();

        for (Token argument : arguments) {

            Symbol symbol = symbolTable.lookup(argument.getLexeme());
            if (symbol == null) {
                error("Variable '" + argument.getLexeme() + "' is not declared", node.getPosition());
            }
            if (!symbol.isInitialized()) {
                error("Variable '" + argument.getLexeme() + "' is not initialized", node.getPosition());
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

    private void evaluateCondition(ExpressionNode condition) {

    }

    // Visit a while node
    private void visitWhileNode(WhileNode node) {
        evaluateCondition(node.getCondition());

        for (StatementNode statement : node.getStatements()) {
            visit(statement);
        }
    }

    // Report an error
    private void error(String message, Position position) {
        System.err.println("Error at " + position + ": " + message);
    }

    public SymbolTable getInitialSymbolTable() {
        return initialSymbolTable;
    }
}