import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import src.Interpreter;
import src.analyzer.SemanticsAnalyzer;
import src.lexer.Lexer;
import src.nodes.ProgramNode;
import src.parser.Parser;
import src.utils.Token;
import src.nodes.*;

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
            
            for (Token token : tokens) {
                System.out.println(token);
            }

            Parser parser = new Parser(tokens);
            ProgramNode programNode = parser.parse();
            System.out.println(programNode);

            // SemanticsAnalyzer analyzer = new SemanticsAnalyzer(programNode);
            // analyzer.analyze();

            // Interpreter interpreter = new Interpreter(programNode);
            // interpreter.interpret();

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }     
    }
}