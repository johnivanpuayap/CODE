package src.analyzer;

import src.nodes.*;

class SemanticAnalyzer {
    private SymbolTable symbolTable;

    public SemanticAnalyzer() {
        symbolTable = new SymbolTable();
    }

    // Analyze the AST
    public void analyze(ProgramNode programNode) {

        for (VariableDeclarationNode declaration: programNode.getDeclarations()) {

            try {
                symbolTable.insert(new Symbol(declaration.getType(), declaration.getName(), declaration.getValue());
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        for (StatementNode statement : programNode.getStatements()) {
            try {
                visit(statement);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        visit(programNode);
    }

    // Visit an AST node
    private void visit(ProgramNode programNode) throws Exception {
        if (node instanceof VariableDeclarationNode) {
            visitAssignmentNode((AssignmentNode) node);
        } else if (node instanceof State,pdeNode) {
            visitVariableNode((VariableDeclarationNode) node);
        }
    }

    // Visit an assignment node
    private void visitAssignmentNode(AssignmentNode node) throws Exception {
        Symbol symbol = symbolTable.lookup(node.variable);
        if (symbol == null) {
            throw new Exception("Variable '" + node.variable + "' is not declared");
        }
        symbol.setValue(evaluate(node.value));
    }

    // Visit a variable node
    private void visitVariableNode(VariableNode node) throws Exception {
        Symbol symbol = symbolTable.lookup(node.name);
        if (symbol == null) {
            throw new Exception("Variable '" + node.name + "' is not declared");
        }
        if (!symbol.isInitialized()) {
            throw new Exception("Variable '" + node.name + "' is not initialized");
        }
        // Additional checks and actions can be performed here
    }

    // Evaluate the value of an expression node
    private Object evaluate(ExpressionNode node) {
        // Dummy implementation for demonstration purposes
        // You would implement actual evaluation logic based on the AST structure
        return null;
    }
}