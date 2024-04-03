import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import src.analyzer.SemanticsAnalyzer;
import src.lexer.Lexer;
import src.nodes.ASTNode;
import src.nodes.DeclarationNode;
import src.nodes.FunctionNode;
import src.nodes.ProgramNode;
import src.nodes.VariableNode;
import src.nodes.StringLiteralNode;
import src.nodes.SpecialCharacterNode;
import src.parser.Parser;
import src.utils.Token;

public class Interpreter {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java Interpreter <input_file>");
            System.exit(1);
        }

        String filePath = args[0];

        if (!filePath.endsWith(".code")) {
            System.err.println("Input file must be a .code file");
            System.exit(1);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            
            String input = sb.toString();
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();

            // Show all tokens
            // for (Token token : tokens) {
            //     System.out.println(token);
            // }

            Parser parser = new Parser(tokens);
            ProgramNode programNode = parser.parse();
            
            SemanticsAnalyzer analyzer = new SemanticsAnalyzer(programNode);
            analyzer.analyze();

            System.out.println("Output:");

            List<DeclarationNode> declarations = programNode.getDeclarations();
            List<FunctionNode> functionCalls = programNode.getFunctionCalls();
            for (FunctionNode functionCall : functionCalls) {
                if (functionCall.getFunctionName() == "DISPLAY") {
                    for (ASTNode node : functionCall.getArguments()) {
                        if (node instanceof StringLiteralNode) {
                            StringLiteralNode stringLiteral = (StringLiteralNode) node;
                            continue;
                        }
                        if (node instanceof VariableNode) {
                            VariableNode variable = (VariableNode) node;
                            for (DeclarationNode declaredVariable : declarations) {
                                if (declaredVariable.getVariableName().equals(variable.getVariableName())) {
                                    System.out.print(declaredVariable.getValue());
                                }
                            }
                            continue;
                        }
                        if (node instanceof SpecialCharacterNode) {
                            SpecialCharacterNode specialCharacter = (SpecialCharacterNode) node;
                            System.out.print(specialCharacter.getValue());
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
    }
}