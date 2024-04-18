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

        for (VariableDeclarationNode declaration: programNode.getDeclarations()) {
            
            Symbol symbol = new Symbol(declaration.getType(), declaration.getName(), declaration.getValue());
            if(!symbolTable.insert(symbol)) {
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
        }
    }

    // Visit an assignment node
    private void visitAssignmentNode(AssignmentNode node) {

        Symbol symbol = symbolTable.lookup(node.getVariable().getName());
        
        visitVariableNode(node.getVariable());

        symbol.setValue(evaluate(node.getExpression()));
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

    // Evaluate the value of an expression node
    private String evaluate(ExpressionNode node) {
        // Dummy implementation for demonstration purposes
        // You would implement actual evaluation logic based on the AST structure
        return null;
    }


    // Report an error
    private void error(String message, Position position) {
        System.err.println("Error at " + position + ": " + message);
    }

    public SymbolTable getInitialSymbolTable() {
        return initialSymbolTable;
    }
}