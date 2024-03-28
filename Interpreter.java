import java.util.List;
public class Interpreter {
    public static void main(String[] args) {
        String input = "BEGIN CODE\n" +
                "Your code here\n" +
                "END CODE";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        parser.parse();
        System.out.println("Program parsed successfully!");
    }
}