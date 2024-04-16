// package src.analyzer;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Set;

// import src.nodes.ASTNode;
// import src.nodes.VariableDeclarationNode;
// import src.nodes.FunctionNode;
// import src.nodes.ExpressionNode;
// import src.nodes.ProgramNode;
// import src.nodes.StatementNode;
// import src.nodes.StringLiteralNode;
// import src.nodes.VariableNode;


// import src.utils.Token;



// public class SemanticsAnalyzer {
//     private ProgramNode program;
//     private Set<String> reservedWords;
//     private Set<String> declaredFunctions;
//     private List<VariableDeclarationNode> declarations;
//     private List<StatementNode> statements;

//     public SemanticsAnalyzer(ProgramNode program) {
//         this.program = program;
//         reservedWords = new HashSet<>();
//         declaredFunctions = new HashSet<>();
//         initializeReservedWords();
//         initializeDeclaredFunctions();
//         declarations = this.program.getDeclarations();
//         statements = this.program.getStatements();
//     }

//     public void analyze() {

//         for (VariableDeclarationNode declaration : declarations) {
//             // Extract information from the DeclarationNode
//             String dataType = declaration.getDataType();
//             String variableName = declaration.getVariableName();
//             String value = declaration.getValue();

//             System.out.println("Data Type: " + dataType);
//             System.out.println("Variable Name: " + variableName);
//             System.out.println("Value: " + value);
            
//             // Check if the variable name is a reserved word
//             if (isReservedWord(variableName)) {
//                 error("Variable name cannot be a reserved word: " + variableName + " at line " + declaration.getPosition().getLine());
//             }
            
//             // Check for valid variable name format
//             if (!isValidVariableName(variableName)) {
//                 error("Invalid variable name at line " + declaration.getPosition().getLine() + ": " + variableName);
//             }
    
//             // Check for valid value based on data type
//             if (!isValidValue(dataType, value)) {
//                 error("Invalid value at line " + declaration.getPosition().getLine() + " for " + dataType + " variable " + variableName + ": " + value);
//             }
//         }
        
//         // for (FunctionNode function: functionCalls) {
//         //     String functionName = function.getFunctionName();

//         //     // Check if function has been declared
//         //     if (!isFunctionDeclared(functionName)) {
//         //         error("Function " + functionName + " has not been declared yet at line " + function.getPosition().getLine() + " position " + function.getPosition().getPosition());
//         //     }

//         //     if (functionName == "DISPLAY") {
//         //         for (ASTNode argument : function.getArguments()) {
//         //             if (argument instanceof StringLiteralNode) {
//         //                 StringLiteralNode stringLiteral = (StringLiteralNode) argument;

//         //             } else if (argument instanceof VariableNode){
//         //                 VariableNode variable = (VariableNode) argument;
//         //                 boolean variableFound = false;
//         //                 for (VariableDeclarationNode declaredVariable : declarations) {
//         //                     if (declaredVariable.getVariableName().equals(variable.getVariableName())) {
//         //                         variableFound = true;
//         //                     }
//         //                 }
//         //                 if (!variableFound) {
//         //                     error("Variable " + variable.getVariableName() + " has not been declared yet at line " + variable.getPosition().getLine() + " position " + variable.getPosition().getPosition());
//         //                 }
//         //             }
//         //         }
//         //     }

//         // }

//         for (StatementNode statement : statements) {

//             // if (statement.hasExpression()) {
//             //     ExpressionNode expression = statement.getExpressionNode();
//             //     List<Token> tokens = expression.getTokens();

//             //     Token leftSide = statement.getLeftSide();

//             //     if(!isDeclared(leftSide.getValue())){
//             //         error("Use of undeclared variable " + leftSide.getValue() + " was not declared " + leftSide.getPosition());
//             //     }


//             //     System.out.println("Checking expression" + expression);

//             //     for (Token token : tokens) {

//             //         System.out.println("Checking Token: " + token.getType() + " " + token.getValue());

//             //         // Check if the variable used is okay
//             //         if (token.getType() == Token.Type.VARIABLE) {

//             //             System.out.println("The token is a " + token.getType());

//             //             String variableName = token.getValue();

//             //             if (!isDeclared(variableName)) {
//             //                 error("Use of undeclared variable " + variableName + " was not declared" + token.getPosition());
//             //             }

//             //             if(!isAssigned(variableName)){
//             //                 error("Variable " + variableName + " used before assignment of value at " + token.getPosition());
//             //             }

                        
//             //             String currDataType = "";
//             //             String currValue = "";
//             //             // Check for data type match
//             //             for(DeclarationNode declaration : declarations) {
//             //                 if(declaration.getVariableName().equals(token.getValue())) {
//             //                     currDataType = declaration.getDataType();
//             //                     currValue = declaration.getValue();
//             //                 }
//             //             }

//             //             System.out.println("Data type: " + currDataType + "Value: " + currValue);
                        
//             //             if(currDataType == "INT" || currDataType == "FLOAT") {
//             //                 try {
//             //                     if(currValue != null) {
//             //                         Integer.parseInt(currValue);
//             //                     }
//             //                 } catch (NumberFormatException e) {
//             //                     if (e.getMessage().contains("out of range")) {
//             //                         error("Invalid value for INT/FLOAT data type. The number is too large: " + currValue);
//             //                     } else {
//             //                         error("Invalid value for INT/FLOAT data type. Expected a number value, but got: " + currValue);
//             //                     }
//             //                 }
//             //             } else {
//             //                 error("Invalid value for INT/FLOAT data type. Expected a number value, but got: " + currValue);
//             //             }

//             //         } else if(token.getType() == Token.Type.VALUE) {    
//             //             String currValue = token.getCurrentValue();
//             //             try {
//             //                 if(currValue != null) {
//             //                     Integer.parseInt(currValue);
//             //                 }
//             //             } catch (NumberFormatException e) {
//             //                 if (e.getMessage().contains("out of range")) {
//             //                     error("Invalid value for INT/FLOAT data type. The number is too large: " + currValue);
//             //                 } else {
//             //                     error("Invalid value for INT/FLOAT data type. Expected a number value, but got: " + currValue);
//             //                 }
//             //             }
                        
//             //         }
                    
//             //     }
//             // } else {

//                 // Check for assignment operations
//                 // Token leftSide = statement.getLeftSide();
//                 // Token rightSide = statement.getRightSide();


//                 // System.out.println("Left Side: " + leftSide);
//                 // System.out.println("Right Side: " + rightSide);
                

//                 // if(!isDeclared(leftSide.getValue())){
//                 //     error("Use of undeclared variable " + leftSide.getValue() + " was not declared " + leftSide.getPosition());
//                 // }

//                 // First let's check if it's a value or a variable
//                 // if(statement.getRightSide().getType() == Token.Type.VALUE){
//                     // System.out.println("The right is an INT");
                    
//                     // for (DeclarationNode declaration : declarations) {
//                     //     if (declaration.getVariableName().equals(leftSide.getValue())) {
//                     //         if(declaration.getDataType().equals("INT")){
//                     //             if(!isValidValue("INT", rightSide.getValue())){
//                     //                 error("Invalid value for INT data type. Expected an integer value, but got: " + rightSide.getValue());
//                     //             }
//                     //             statement.getLeftSide().setCurrentValue(rightSide.getValue());

//                     //             System.out.print("Assigning the value of the " + rightSide.getValue() + " to " + statement.getLeftSide().getCurrentValue());

//                     //         } else if(declaration.getDataType().equals("FLOAT")){
//                     //             if(!isValidValue("FLOAT", rightSide.getValue())){
//                     //                 error("Invalid value for FLOAT data type. Expected a floating-point value, but got: " + rightSide.getValue());
//                     //             }
//                     //             statement.getLeftSide().setCurrentValue(rightSide.getValue());
//                     //         } else if(declaration.getDataType().equals("CHAR")){
//                     //             if(!isValidValue("CHAR", rightSide.getValue())){
//                     //                 error("Invalid value for CHAR data type. Expected a single character enclosed in single quotes, but got: " + rightSide.getValue());
//                     //             }
//                     //             statement.getLeftSide().setCurrentValue(rightSide.getValue());
//                     //         } else if(declaration.getDataType().equals("BOOL")){
//                     //             if(!isValidValue("BOOL", rightSide.getValue())){
//                     //                 error("Invalid value for BOOL data type. Expected \"TRUE\" or \"FALSE\" (case sensitive), but got: " + rightSide.getValue());
//                     //             }
//                     //             statement.getLeftSide().setCurrentValue(rightSide.getValue());
//                     //         }
//                     //     }
//                     // }
                
//                 } else {
                    
//                     // if(!isDeclared(statement.getRightSide().getValue())){
//                     //     error("Use of undeclared variable " + statement.getRightSide().getValue() + " was not declared " + statement.getRightSide().getPosition());
//                     // } else{
//                     //     // let's find the data types or left side then the right side
//                     //     String rightDataType = "1";
//                     //     String leftDataType = "2";
//                     //     String rightValue = " ";
                        
//                     //     for(DeclarationNode declaration : declarations) {
//                     //         if(declaration.getVariableName().equals(leftSide.getValue())) {
//                     //             leftDataType = declaration.getDataType();
//                     //         }
//                     //         else if(declaration.getVariableName().equals(rightSide.getValue())) {
//                     //             rightDataType = declaration.getDataType();
//                     //             rightValue = declaration.getValue();
//                     //         }

//                     //     }
   
//                     //     if( rightDataType == leftDataType) {
//                     //         System.out.println("Assigning value of " + rightValue + " to " + leftSide.getValue());
                            
                            
//                     //         statement.getLeftSide().setCurrentValue(rightValue);
//                     //         // Update the value in the declarations
                            
//                     //         for(DeclarationNode declaration: declarations) {
//                     //             if(declaration.getVariableName().equals(leftSide.getValue())) {
//                     //                 declaration.setValue(rightValue);
//                     //             }
//                     //         }


//                     //     } else {
//                     //         error("Data type mismatch at assignment of variable " + leftSide.getPosition());
//                     //     }
//                     }
//                 }
//             }
//         }
//     }
    

//     // Initialization of Variables

//     private boolean isReservedWord(String variableName) {
//         return reservedWords.contains(variableName);
//     }
    
//     private boolean isValidVariableName(String variableName) {
//         return variableName.matches("[a-z_][a-zA-Z0-9_]*");
//     }
    
//     private boolean isValidValue(String dataType, String value) {
//         if (value == null) {
//             return true;
//         }
        
//         switch(dataType) {
//             case "INT":
//                 try {
//                     if(value != null) {
//                         Integer.parseInt(value);
//                         return value.matches("-?\\d+"); // The value fits within 4 bytes
//                     }
                   
//                 } catch (NumberFormatException e) {
//                     if (e.getMessage().contains("out of range")) {
//                         error("Invalid value for INT data type. The number is too large to fit in 4 bytes: " + value);
//                     } else {
//                         error("Invalid value for INT data type. Expected an integer value, but got: " + value);
//                     }
//                 }
//             case "FLOAT":
//                 try {
//                     if(value != null) {
//                         Float.parseFloat(value);
//                         return true;
//                     }
//                 } catch (NumberFormatException e) {
//                     if (e.getMessage().contains("out of range")) {
//                         error("Invalid value for FLOAT data type. The number is too large: " + value);
//                     } else {
//                         error("Invalid value for FLOAT data type. Expected a floating-point value, but got: " + value);
//                     }
//                 }
                
//             case "CHAR":
//                 if (value != null) {
//                     if (value.matches("'.'")) {
//                         return true;
//                     } else {
//                         error("Invalid value for CHAR data type. Expected a single character enclosed in single quotes, but got: " + value);
//                     }
//                 }
//             case "BOOL":
//                 if (value != null) {
//                     if (value.equals("TRUE") || value.equals("FALSE")) {
//                         return true;
//                     } else {
//                         error("Invalid value for BOOL data type. Expected \"TRUE\" or \"FALSE\" (case sensitive), but got: " + value);
//                     }
//                 }
//             default:
//                 return false;
//         }
//     }


//     // Use of Variables
//     private boolean isDeclared(String variableName) {
//         for (DeclarationNode declaration : declarations) {
//             if (declaration.getVariableName().equals(variableName)) {
//                 return true;
//             }
//         }

//         return false;
//     }

//     // Assignment of Variables
//     private boolean isAssigned(String variableName) {
//         for (DeclarationNode declaration : declarations) {
//             if (declaration.getVariableName().equals(variableName)) {
//                 return declaration.getValue() != null;
//             }
//         }

//         return false;
//     }

//     private boolean isFunctionDeclared(String functionName) {
//         return declaredFunctions.contains(functionName);
//     }

//     // Method to handle errors
//     private void error(String message) {
//         throw new RuntimeException(message);
//     }

//     private void initializeReservedWords() {
//         // Add all reserved words to the set
//         reservedWords.add("BEGIN");
//         reservedWords.add("CODE");
//         reservedWords.add("END");
//         reservedWords.add("INT");
//         reservedWords.add("CHAR");
//         reservedWords.add("BOOL");
//         reservedWords.add("FLOAT");
//         reservedWords.add("DISPLAY");
//         reservedWords.add("SCAN");
//         reservedWords.add("IF");
//         reservedWords.add("ELSE");
//         reservedWords.add("WHILE");
//         reservedWords.add("TRUE");
//         reservedWords.add("FALSE");
//     }

//     private void initializeDeclaredFunctions() {
//         declaredFunctions.add("DISPLAY");
//         declaredFunctions.add("SCAN");
//     }
// }