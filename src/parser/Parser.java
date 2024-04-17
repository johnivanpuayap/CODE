package src.parser;
import java.util.ArrayList;
import java.util.List;
import src.utils.Token;
import src.utils.Position;
import src.nodes.ASTNode;
import src.nodes.ProgramNode;
import src.nodes.SpecialCharacterNode;
import src.nodes.DeclarationNode;
import src.nodes.StatementNode;
import src.nodes.StringLiteralNode;
import src.nodes.VariableNode;
import src.nodes.ExpressionNode;
import src.nodes.FunctionNode;
import src.nodes.ScanStatementNode;

public class Parser {
    private final List<Token> tokens;
    private int currentTokenIndex = 0;


    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }

    public ProgramNode parse() {
        return parseProgram();
    }

    private ProgramNode parseProgram() {
        if (!match(Token.Type.BEGIN_CODE)) {
            error("Expected BEGIN CODE", peek());
        }

        List<StatementNode> statements = new ArrayList<>();

        // Parse statements within the program
        while (!isAtEnd() && peek().getType() != Token.Type.END_CODE) {
            statements.add(parseStatement());
        }

        if (!match(Token.Type.END_CODE)) {
            error("Expected END CODE", peek());
        }

        return null;
    }

    private StatementNode parseStatement() {
        Token currentToken = peek();

        // Check if the statement is a SCAN statement
        if (currentToken.getType() == Token.Type.SCAN) {
            return parseScanStatement();
        }

        // If it's not a SCAN statement, assume it's a regular assignment statement
        // You can implement parseAssignmentStatement() according to your language's syntax
        return parseAssignmentStatement();
    }

    private StatementNode parseScanStatement() {
        consume(Token.Type.SCAN); // Consume the SCAN keyword
        consume(Token.Type.COLON); // Consume the colon ":" after SCAN

        List<String> identifiers = new ArrayList<>();

        // Parse the list of identifiers after the colon
        while (match(Token.Type.IDENTIFIER)) {
            identifiers.add(previous().getValue());
            // Check for comma to parse multiple identifiers
            if (!match(Token.Type.COMMA)) {
                break; // Exit loop if no comma found
            }
        }

        // Create a SCAN statement node with the list of identifiers
        return new ScanStatementNode(identifiers);
    }

    // Implementation of parseAssignmentStatement() depends on your language's syntax
    private StatementNode parseAssignmentStatement() {
        // Implement parsing for assignment statements here
        return null;
    }

    private void error(String message, Token token) {
        System.err.println("Syntax error " + token.getPosition() + ": " + message);
        System.exit(1);
    }

    private Token peek() {
        return tokens.get(currentTokenIndex);
    }

    private Token previous() {
        return tokens.get(currentTokenIndex - 1);
    }

    private void consume(Token.Type type) {
        if (match(type)) {
            // If the next token matches the expected type, consume it
            return;
        }
        // If the next token does not match the expected type, report an error
        error("Expected " + type + " token", peek());
    }

    private boolean isAtEnd() {
        return currentTokenIndex >= tokens.size();
    }

    private boolean match(Token.Type type) {
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() == type) {
            currentTokenIndex++;
            return true;
        }
        return false;
    }
}
