import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import src.analyzer.SemanticsAnalyzer;
import src.lexer.Lexer;
import src.nodes.ProgramNode;
import src.parser.Parser;
import src.utils.Token;
/*
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

            for (Token token : tokens) {
                System.out.println(token);
            }

            Parser parser = new Parser(tokens);
            ProgramNode programNode = parser.parse();

            SemanticsAnalyzer analyzer = new SemanticsAnalyzer(programNode);
            analyzer.analyze();

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }     
    }
}*/

public class Interpreter {
    public static void main(String[] args) {
        String input = "BEGIN CODE\n" +
                "    INT aaa1=10, b=100, s2, a2=1\n" +
                "    CHAR qa1='a'\n" +
                "    FLOAT qw2=23.0f\n" +
                "    BOOL oiew2=\"FALSE\", ewww=\"TRUE\"\n" +
                "    [[]\n" +
                "    [/]\n" +
                "    []]\n" +
                "    #This is a comment\n" +
                "    SCAN var1, var2\n" +
                "END CODE";

        // Create a Lexer instance
        Lexer lexer = new Lexer(input);

        // Tokenize the input
        List<Token> tokens = lexer.tokenize();
        for (Token token : tokens) {
            if(token.getType()==Token.Type.ESCAPE_CODE){
                System.out.println("Escape Code: " + token.getValue());
            }
            if (token.getType() == Token.Type.VALUE) {
                System.out.println("Input Value: " + token.getValue());
            }

            if (token.getType() == Token.Type.SCAN) {
                System.out.println("Token Type: " + token.getValue());
            }

        }
    }
}
