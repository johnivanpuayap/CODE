package src.nodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ScanStatementNode extends StatementNode {
    private List<String> identifiers;

    public ScanStatementNode(List<String> identifiers) {
        super();
        this.identifiers = identifiers;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }


    @Override
    public void execute() {
        // Create a symbol table to store the variable values
        Map<String, Object> symbolTable = new HashMap<>();

        Scanner scanner = new Scanner(System.in);

        for (String identifier : identifiers) {
            System.out.println(identifier + ": ");
            String input = scanner.nextLine();

            // Store the input value in the symbol table
            symbolTable.put(identifier, input);

            //Print the input value for demonstration purposes
//            System.out.println("Value for " + identifier + ": " + input);
        }

        scanner.close();

        // Optionally, you could print the contents of the symbol table to verify that the values were stored correctly
        System.out.println("Symbol table: " + symbolTable);
    }
}
