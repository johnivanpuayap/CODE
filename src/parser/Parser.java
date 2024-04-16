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
        if(!match(Token.Type.BEGIN_CODE)) {
            error("Expected BEGIN CODE", peek());
        }

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
