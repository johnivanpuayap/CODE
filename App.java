import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import src.Interpreter;
import src.analyzer.SemanticAnalyzer;
import src.lexer.Lexer;
import src.nodes.ProgramNode;
import src.parser.Parser;
import src.utils.SymbolTable;
import src.utils.Token;

public class App {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java App <input_file>");
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

            Parser parser = new Parser(tokens);
            ProgramNode programNode = parser.parse();

            SemanticAnalyzer analyzer = new SemanticAnalyzer(programNode);
            analyzer.analyze();

            SymbolTable symbolTable = analyzer.getInitialSymbolTable();

            Interpreter interpreter = new Interpreter(programNode, symbolTable);
            interpreter.interpret();

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
    }
}