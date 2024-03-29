import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SemanticsAnalyzer {
    private ProgramNode program;
    private Set<String> reservedWords = new HashSet<>();

    public SemanticsAnalyzer(ProgramNode program) {
        this.program = program;
        initializeReservedWords();
    }

    public void analyze() {
        List<DeclarationNode> declarations = program.getDeclarations();
        
        for (DeclarationNode declaration : declarations) {
            // Extract information from the DeclarationNode
            String dataType = declaration.getDataType();
            String variableName = declaration.getVariableName();
            String value = declaration.getValue();
            
            // Check if the variable name is a reserved word
            if (isReservedWord(variableName)) {
                error("Variable name cannot be a reserved word: " + variableName + " at line " + declaration.getPosition().getLine());
            }
            
            // Check for valid variable name format
            if (!isValidVariableName(variableName)) {
                error("Invalid variable name at line " + declaration.getPosition().getLine() + ": " + variableName);
            }
    
            // Check for valid value based on data type
            if (!isValidValue(dataType, value)) {
                error("Invalid value at line " + declaration.getPosition().getLine() + " for " + dataType + " variable " + variableName + ": " + value);
            }
            
            // Perform semantics analysis and execution logic for each declaration
            // For example, you might evaluate expressions, check variable scopes, etc.
        }
    }
    
    private boolean isReservedWord(String variableName) {
        return reservedWords.contains(variableName);
    }
    
    private boolean isValidVariableName(String variableName) {
        // Check if the variable name matches the pattern
        return variableName.matches("[a-z_][a-zA-Z0-9_]*");
    }
    
    private boolean isValidValue(String dataType, String value) {
        return value.isEmpty() || (dataType.equals("INT") && value.matches("-?\\d+"));
    }

    // Method to handle errors
    private void error(String message) {
        throw new RuntimeException(message);
    }

    private void initializeReservedWords() {
        // Add all reserved words to the set
        reservedWords.add("BEGIN");
        reservedWords.add("CODE");
        reservedWords.add("END");
        reservedWords.add("INT");
        reservedWords.add("CHAR");
        reservedWords.add("BOOL");
        reservedWords.add("FLOAT");
        reservedWords.add("DISPLAY");
        reservedWords.add("SCAN");
        reservedWords.add("IF");
        reservedWords.add("ELSE");
        reservedWords.add("WHILE");
        reservedWords.add("TRUE");
        reservedWords.add("FALSE");
    }
}