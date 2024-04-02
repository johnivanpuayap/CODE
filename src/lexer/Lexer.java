package src.lexer;
import java.util.List;
import java.util.ArrayList;
import src.utils.Token;
import src.utils.Position;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.*;

// Lexer or Lexical Analyzer class to tokenize the input program
public class Lexer {
    private String input;
    private int counter;
    private int currentIndent;
    private Position position;
    private Scanner scanner;

    public Lexer(String input) {
        this.input = input;
        this.counter = 0;
        this.position = new Position(1, 0);
        this.currentIndent = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        try {
            while (counter < input.length()) {
                scanner = new Scanner(System.in);
                
                char currentChar = input.charAt(counter);
                
                //Print Position Currently Checking
                System.out.println("Currently Checking: " + position);
                System.out.println("Current Char: " + currentChar);

                // Skip whitespaces
                if (isWhitespace(currentChar)) {
                    if (currentChar == '\n') {
                        position.setLine(position.getLine() + 1);
                        position.setPosition(1);
                    } else {
                        position.setPosition(position.getPosition() + 1);
                    }

                    counter++;    
                    continue;
                }

                // Skip comments
                if (currentChar == '#') {
                    while (counter < input.length() && input.charAt(counter) != '\n') {
                        counter++;
                        position.setPosition(position.getPosition() + 1);
                    }

                    if (counter < input.length() && input.charAt(counter) == '\n') {
                        position.setLine(position.getLine() + 1);
                        position.setPosition(1);
                        counter++;
                    }

                    continue;
                }

                if (input.startsWith("BEGIN CODE", counter)) {
                    tokens.add(new Token(Token.Type.BEGIN_CODE, "BEGIN CODE", position));
                    position.setPosition(position.getPosition() + "BEGIN CODE".length());
                    counter += "BEGIN CODE".length();
                    
                    // Will be used to check for indentation inside BEGIN CODE and END CODE block
                    int counterForIndentation = counter;
                    Position indentCheck = new Position(position.getLine(), position.getPosition());

                    // Check if there is a newline after "BEGIN CODE"
                    if (input.charAt(counterForIndentation) != '\n') {
                        throw new RuntimeException("Newline required after BEGIN CODE at Line " + indentCheck.getLine() + ", Position " + indentCheck.getPosition());
                    }
                    
                    int[] result = moveToNextLine(counterForIndentation, indentCheck.getLine(), indentCheck.getPosition());
                    
                    counterForIndentation = result[0];
                    indentCheck.setLine(result[1]);
                    indentCheck.setPosition(result[2]);
                    
                    // Ensure proper indentation after "BEGIN CODE"
                    if (counterForIndentation < input.length() && isWhitespace(input.charAt(counter))) {
                        currentIndent = findIndentLevel(counterForIndentation);

                        if (currentIndent == 0 && input.charAt(counterForIndentation) != '#'){
                            throw new RuntimeException("Indentation error after BEGIN CODE at Line " + indentCheck.getLine() + ", Position " + indentCheck.getPosition());
                        }
                    } else {
                        throw new RuntimeException("Indentation required after BEGIN CODE at Line "  + indentCheck.getLine() + ", Position " + indentCheck.getPosition());
                    }

                    // Check indentation for subsequent lines until "END CODE"
                    while (!input.startsWith("END CODE", counterForIndentation)) {
                        int indentLevel = findIndentLevel(counterForIndentation);

                        if (indentLevel != currentIndent && input.charAt(counterForIndentation) != '#') {
                            throw new RuntimeException("Improper indentation inside BEGIN CODE at Line "  + indentCheck.getLine() + ", Position " + indentCheck.getPosition());
                        }
                        
                        result = moveToNextLine(counterForIndentation, indentCheck.getLine(), indentCheck.getPosition());

                        counterForIndentation = result[0];
                        indentCheck.setLine(result[1]);
                        indentCheck.setPosition(result[2]);
                    }
                }

                if (input.startsWith("END CODE", counter)) {
                    tokens.add(new Token(Token.Type.END_CODE, "END CODE", position));
                    position.setPosition(position.getPosition() + "END CODE".length());
                    counter += "END CODE".length();
                    continue;
                }
                
                //added the INT Datatype
                if (input.startsWith("INT", counter)) {
                    // Tokenize INT declaration
                    tokens.add(new Token(Token.Type.DATA_TYPE, "INT", position));
                    position.setPosition(position.getPosition() + "INT".length());
                    counter += "INT".length();

                    // Parse variable names and values
                    while (counter < input.length() && input.charAt(counter) != '\n') {

                        // Skip whitespace
                        while (counter < input.length() && isWhitespace(input.charAt(counter))) {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }

                        // Parse variable name
                        StringBuilder variableName = new StringBuilder();
                        while (counter < input.length() && input.charAt(counter) != ',' && input.charAt(counter) != '=' && input.charAt(counter) != '\n') {
                            variableName.append(input.charAt(counter));
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }

                        if (!isValidVariableName(variableName.toString())) {
                            System.err.println("Invalid variable name at Line " + position.getLine() + ", Position " + position.getPosition());
                            System.exit(1);
                        }

                        // Add variable token
                        tokens.add(new Token(Token.Type.VARIABLE, variableName.toString(), position));
                        System.out.println("Variable name found at Line " + position.getLine() + ", Position " + position.getPosition());

                        // Check for optional initialization
                        if (counter < input.length() && input.charAt(counter) == '=') {
                            // Tokenize assignment operator
                            System.out.println("Assignment operator found at Line " + position.getLine() + ", Position " + position.getPosition());
                            tokens.add(new Token(Token.Type.ASSIGNMENT, "=", position));
                            position.setPosition(position.getPosition() + 1);
                            counter++;

                            // Parse value
                            StringBuilder value = new StringBuilder();
                            while (counter < input.length() && !isWhitespace(input.charAt(counter)) && input.charAt(counter) != ',') {
                                value.append(input.charAt(counter));
                                position.setPosition(position.getPosition() + 1);
                                counter++;
                            }
                            tokens.add(new Token(Token.Type.VALUE, value.toString(), position));
                        }

                        if (counter < input.length() && input.charAt(counter) == ',') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;

                            // Since there is a comma, we expect another variable name, so we create a new data type token
                            tokens.add(new Token(Token.Type.DATA_TYPE, "INT", position));
                        } else {
                            break;
                        }
                    }
                    continue;
                }

                // Tokenize FLOAT declaration
                if (input.startsWith("FLOAT", counter)) {
                    // Tokenize FLOAT declaration
                    tokens.add(new Token(Token.Type.DATA_TYPE, "FLOAT", position));
                    position.setPosition(position.getPosition() + "FLOAT".length());
                    counter += "FLOAT".length();

                    while (counter < input.length() && input.charAt(counter) != '\n') {
                        // Skip whitespace
                        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }

                        // Parse variable name
                        StringBuilder variableName = new StringBuilder();
                        while (counter < input.length() && input.charAt(counter) != ',' && input.charAt(counter) != '=' && input.charAt(counter) != '\n') {
                            variableName.append(input.charAt(counter));
                            counter++;
                        }

                        if (!isValidVariableName(variableName.toString())) {
                            System.err.println("Invalid variable name at Line " + position.getLine() + ", Position " + position.getPosition());
                            System.exit(1);
                        }

                        // Add variable token
                        tokens.add(new Token(Token.Type.VARIABLE, variableName.toString(), position));
                        System.out.println("Variable name found at Line " + position.getLine() + ", Position " + position.getPosition());

                        // Check for optional initialization
                        if (counter < input.length() && input.charAt(counter) == '=') {
                            // Tokenize assignment operator
                            System.out.println("Assignment operator found at Line " + position.getLine() + ", Position " + position.getPosition());
                            tokens.add(new Token(Token.Type.ASSIGNMENT, "=", position));
                            position.setPosition(position.getPosition() + 1);
                            counter++;

                            // Parse value
                            StringBuilder value = new StringBuilder();
                            // Skip leading whitespace
                            while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                                position.setPosition(position.getPosition() + 1);
                                counter++;
                            }
                            // Check if the value starts with a digit or '.'
                            while (counter < input.length() && !Character.isWhitespace(input.charAt(counter)) && input.charAt(counter) != ',') {
                                value.append(input.charAt(counter));
                                counter++;
                            }
                            // Check if the value ends with 'f' suffix
                            if (value.length() > 0 && value.charAt(value.length() - 1) == 'f') {
                                // Add the float value token
                                tokens.add(new Token(Token.Type.VALUE, value.toString(), position));
                            } else {
                                // Throw an exception if 'f' suffix is missing
                                throw new RuntimeException("Missing 'f' suffix for float literal at Line " + position.getLine() + ", Position " + position.getPosition());
                            }
                        }

                        // Skip trailing whitespace and comma
                        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }

                        if (counter < input.length() && input.charAt(counter) == ',') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;

                            // Since there is a comma, we expect another variable name, so we create a new data type token
                            tokens.add(new Token(Token.Type.DATA_TYPE, "FLOAT", position));
                        } else {
                            break;
                        }
                    }
                    continue;
                }

                //added the CHAR datatype
                if (input.startsWith("CHAR", counter)) {
                    // Tokenize CHAR declaration
                    tokens.add(new Token(Token.Type.DATA_TYPE, "CHAR", position));
                    position.setPosition(position.getPosition() + "CHAR".length());
                    counter += "CHAR".length();

                    // Similarly, parse variable names and values as done for INT
                    // You'd follow the same pattern as for INT, but with "CHAR" instead
                    // Parse variable names and values
                    while (counter < input.length() && input.charAt(counter) != '\n') {

                        // Skip whitespace
                        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }

                        // Parse variable name
                        StringBuilder variableName = new StringBuilder();
                        while (counter < input.length() && input.charAt(counter) != ',' && input.charAt(counter) != '=' && input.charAt(counter) != '\n') {
                            variableName.append(input.charAt(counter));
                            counter++;
                        }

                        if (!isValidVariableName(variableName.toString())) {
                            System.err.println("Invalid variable name at Line " + position.getLine() + ", Position " + position.getPosition());
                            System.exit(1);
                        }

                        // Add variable token
                        tokens.add(new Token(Token.Type.VARIABLE, variableName.toString(), position));
                        System.out.println("Variable name found at Line " + position.getLine() + ", Position " + position.getPosition());

                        // Check for optional initialization
                        if (counter < input.length() && input.charAt(counter) == '=') {
                            // Tokenize assignment operator
                            System.out.println("Assignment operator found at Line " + position.getLine() + ", Position " + position.getPosition());
                            tokens.add(new Token(Token.Type.ASSIGNMENT, "=", position));
                            position.setPosition(position.getPosition() + 1);
                            counter++;

                            // Parse value
                            StringBuilder value = new StringBuilder();
                            if (input.charAt(counter) == '\'') { // Check if the value starts with a single quote
                                value.append(input.charAt(counter)); // Append the single quote to the value
                                position.setPosition(position.getPosition() + 1);
                                counter++;

                                // Parse the character
                                if (counter < input.length()) {
                                    value.append(input.charAt(counter));
                                    position.setPosition(position.getPosition() + 1);
                                    counter++;
                                } else {
                                    throw new RuntimeException("Invalid character literal at Line " + position.getLine() + ", Position " + position.getPosition());
                                }

                                // Check if the character literal is closed with a single quote
                                if (counter < input.length() && input.charAt(counter) == '\'') {
                                    value.append(input.charAt(counter)); // Append the closing single quote to the value
                                    position.setPosition(position.getPosition() + 1);
                                    counter++;
                                } else {
                                    throw new RuntimeException("Unclosed character literal at Line " + position.getLine() + ", Position " + position.getPosition());
                                }

                                // Ensure that the value is only one character
                                if (value.length() != 3) { // A single-character literal should have length 3 (including the single quotes)
                                    throw new RuntimeException("Invalid character literal at Line " + position.getLine() + ", Position " + position.getPosition());
                                }

                                // Add the CHAR value token
                                tokens.add(new Token(Token.Type.VALUE, value.toString(), position));
                            } else {
                                throw new RuntimeException("Invalid character literal at Line " + position.getLine() + ", Position " + position.getPosition());
                            }
                        }


                        // Skip trailing whitespace and comma
                        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }

                        if (counter < input.length() && input.charAt(counter) == ',') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;

                            // Since there is a comma, we expect another variable name, so we create a new data type token
                            tokens.add(new Token(Token.Type.DATA_TYPE, "CHAR", position));
                        } else {
                            break;
                        }
                    }
                    continue;
                }

                // Tokenize BOOLEAN declaration
                if (input.startsWith("BOOL", counter)) {
                    // Tokenize BOOLEAN declaration
                    tokens.add(new Token(Token.Type.DATA_TYPE, "BOOL", position));
                    position.setPosition(position.getPosition() + "BOOL".length());
                    counter += "BOOL".length();

                    // Parse variable names and values
                    while (counter < input.length() && input.charAt(counter) != '\n') {

                        // Skip whitespace
                        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }

                        // Parse variable name
                        StringBuilder variableName = new StringBuilder();
                        while (counter < input.length() && input.charAt(counter) != ',' && input.charAt(counter) != '=' && input.charAt(counter) != '\n') {
                            variableName.append(input.charAt(counter));
                            counter++;
                        }

                        if (!isValidVariableName(variableName.toString())) {
                            System.err.println("Invalid variable name at Line " + position.getLine() + ", Position " + position.getPosition());
                            System.exit(1);
                        }

                        // Add variable token
                        tokens.add(new Token(Token.Type.VARIABLE, variableName.toString(), position));
                        System.out.println("Variable name found at Line " + position.getLine() + ", Position " + position.getPosition());

                        // Check for optional initialization
                        if (counter < input.length() && input.charAt(counter) == '=') {
                            // Tokenize assignment operator
                            System.out.println("Assignment operator found at Line " + position.getLine() + ", Position " + position.getPosition());
                            tokens.add(new Token(Token.Type.ASSIGNMENT, "=", position));
                            position.setPosition(position.getPosition() + 1);
                            counter++;

                            // Skip leading whitespace
                            while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                                position.setPosition(position.getPosition() + 1);
                                counter++;
                            }

                            // Check if the boolean value is enclosed in quotation marks
                            if (input.charAt(counter) == '"') {
                                counter++; // Move past the opening quotation mark
                                int valueStart = counter;
                                while (counter < input.length() && input.charAt(counter) != '"') {
                                    counter++;
                                }
                                if (counter == input.length()) {
                                    throw new RuntimeException("Missing closing quotation mark for boolean value at Line " + position.getLine() + ", Position " + position.getPosition());
                                }
                                // Tokenize the boolean value enclosed in quotation marks
                                tokens.add(new Token(Token.Type.VALUE, input.substring(valueStart, counter), position));
                                position.setPosition(position.getPosition() + counter - valueStart);
                                counter++; // Move past the closing quotation mark
                            } else {
                                // Parse value without quotation marks
                                if (input.startsWith("TRUE", counter)) {
                                    // Tokenize boolean value true
                                    tokens.add(new Token(Token.Type.VALUE, "TRUE", position));
                                    position.setPosition(position.getPosition() + "TRUE".length());
                                    counter += "TRUE".length();
                                } else if (input.startsWith("FALSE", counter)) {
                                    // Tokenize boolean value false
                                    tokens.add(new Token(Token.Type.VALUE, "FALSE", position));
                                    position.setPosition(position.getPosition() + "FALSE".length());
                                    counter += "FALSE".length();
                                } else {
                                    // Invalid boolean value
                                    throw new RuntimeException("Invalid boolean value at Line " + position.getLine() + ", Position " + position.getPosition());
                                }
                            }
                        }

                        // Skip trailing whitespace and comma
                        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }

                        if (counter < input.length() && input.charAt(counter) == ',') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;

                            // Since there is a comma, we expect another variable name, so we create a new data type token
                            tokens.add(new Token(Token.Type.DATA_TYPE, "BOOL", position));
                        } else {
                            break;
                        }
                    }
                    continue;
                }

                //Tokenize SCAN
                if (input.startsWith("SCAN", counter)) {
                    counter += "SCAN".length();
                    tokens.add(new Token(Token.Type.SCAN, "SCAN", position));
                    position.setPosition(position.getPosition() + "SCAN".length());

                    // Parse variable names and values
                    while (counter < input.length() && input.charAt(counter) != '\n') {

                        skipWhiteSpace();
                        // Parse variable name
                        StringBuilder variableName = new StringBuilder();
                        while (counter < input.length() && input.charAt(counter) != ',' && input.charAt(counter) != '=' && input.charAt(counter) != '\n') {
                            variableName.append(input.charAt(counter));
                            counter++;
                        }

                        if (!isValidVariableName(variableName.toString())) {
                            System.err.println("Invalid variable name at Line " + position.getLine() + ", Position " + position.getPosition());
                            System.exit(1);
                        }

                        // Add variable token
                        tokens.add(new Token(Token.Type.VARIABLE, variableName.toString(), position));
                        System.out.println("Variable name found at Line " + position.getLine() + ", Position " + position.getPosition());

                        // Get user input
                        Scanner scanner = new Scanner(System.in);
                        System.out.print("Enter value for " + variableName + ": ");
                        String userInput = scanner.nextLine();
                        scanner.close();

                        // Determine data type of user input
                        if (userInput.matches("^[a-zA-Z]$")) {
                            tokens.add(new Token(Token.Type.VALUE, userInput, position));
                        } else if (userInput.matches("^-?\\d+$")) {
                            tokens.add(new Token(Token.Type.VALUE, userInput, position));
                        } else if (userInput.matches("^-?\\d+\\.\\d+$")) {
                            tokens.add(new Token(Token.Type.VALUE, userInput + "f", position));
                        } else {
                            System.err.println("Invalid input at Line " + position.getLine() + ", Position " + position.getPosition());
                            System.exit(1);
                        }

                        // Skip whitespace
                        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }

                        if (counter < input.length() && input.charAt(counter) == ',') {
                            position.setPosition(position.getPosition() + 1);
                            counter++;

                            // Since there is a comma, we expect another variable name, so we create a new data type token
                            tokens.add(new Token(Token.Type.DATA_TYPE, "FLOAT", position));
                        } else {
                            break;
                        }
                    }
                    continue;
                }

                // Tokenize escape code with square brackets
                if (input.charAt(counter) == '[') {
                    // Tokenize escape code
                    StringBuilder escapeCode = new StringBuilder();
                    counter++; // Move past the opening square bracket
                    while (counter < input.length() && input.charAt(counter) != '\n') {
                        escapeCode.append(input.charAt(counter));
                        counter += 2;
                    }
                    if (counter == input.length()) {
                        throw new RuntimeException("Missing closing square bracket ']' for escape code at Line " + position.getLine() + ", Position " + position.getPosition());
                    }
                    tokens.add(new Token(Token.Type.ESCAPE_CODE, escapeCode.toString(), position));
                    counter++; // Move past the closing square bracket
                    continue;
                }


                // Tokenize statements, this should always be the last condition
                if (Character.isLetter(input.charAt(counter))) {

                    System.out.println("Found a Statement");
                    // Tokenize variable name
                    StringBuilder variableName = new StringBuilder();
                    Position variablePosition = new Position(position.getLine(), position.getPosition());

                    while (counter < input.length() && input.charAt(counter) != '=' && input.charAt(counter) != '\n' && input.charAt(counter) != ' ') {
                        variableName.append(input.charAt(counter));
                        position.setPosition(position.getPosition() + 1);
                        counter++;
                    }

                    System.out.println("Variable name " + variableName + " found at " + variablePosition);

                    if (!isValidVariableName(variableName.toString())) {
                        System.err.println("Invalid variable name " + position);
                        System.exit(1);
                    }

                    
                    System.out.println("Adding a variable token at " + variablePosition);
                    tokens.add(new Token(Token.Type.VARIABLE, variableName.toString(), variablePosition));
                    

                    skipWhiteSpace();

                    // Tokenize assignment operator
                    if (counter < input.length() && input.charAt(counter) == '=') {
                        
                        System.out.println("Adding an assignment operator at " + position);
                        tokens.add(new Token(Token.Type.ASSIGNMENT, "=", position));
                        position.setPosition(position.getPosition() + 1);
                        counter++;
                    } else {
                        throw new RuntimeException("Missing assignment operator at" + position);
                    }

                    skipWhiteSpace();

                    // Examples 
                    // x = 5 + 10
                    // x = y = 5
                    
                    // Loop through the expression
                    while(counter < input.length() && input.charAt(counter) != '\n' && input.charAt(counter) != '#'){
                        
                        // Store the value of expression or variable
                        StringBuilder value = new StringBuilder();
                        variablePosition.setLine(position.getLine());
                        variablePosition.setPosition(position.getPosition());
                        while (counter < input.length() && input.charAt(counter) != '\n' && input.charAt(counter) != '#' && input.charAt(counter) !=  '=') {
                            value.append(input.charAt(counter));
                            position.setPosition(position.getPosition() + 1);
                            counter++;
                        }

                        System.out.println("Value: " + value.toString());
                        System.out.println(input.charAt(counter) == '\n');

                        if(input.charAt(counter) == '\n') {
                            
                            System.out.println("Found an expression: " + value.toString());

                            String numPattern = "-?\\d+";
                            String operatorPattern = "[+\\-*/]";
                            String variablePattern = "[a-zA-Z]+\\w*"; // Matches variable names (letters followed by optional alphanumeric characters or underscores)
                            String parenthesesPattern = "[()]";

                            Pattern pattern = Pattern.compile(operatorPattern + "|" + numPattern + "|" + variablePattern + "|" + parenthesesPattern);
                            Matcher matcher = pattern.matcher(value.toString());
                            int currentPosition = variablePosition.getPosition();

                            while (matcher.find()) {
                                String component = matcher.group();
                                Position componentPosition = new Position(variablePosition.getLine(), currentPosition);

                                if (component.matches(operatorPattern)) { // Match arithmetic operators
                                    System.out.println("Found an operator: " + component);
                                    tokens.add(new Token(Token.Type.OPERATOR, component, componentPosition));
                                } else if (component.matches(numPattern)) { // Match numerical values
                                    System.out.println("Found a number: " + component);
                                    tokens.add(new Token(Token.Type.VALUE, component, componentPosition));
                                } else if (component.matches(variablePattern)) { // Match variable names
                                    System.out.println("Found a variable: " + component);
                                    tokens.add(new Token(Token.Type.VARIABLE, component, componentPosition));
                                } else if (component.matches(parenthesesPattern)) { // Match parentheses
                                    System.out.println("Found a parentheses: " + component);
                                    tokens.add(new Token(Token.Type.PARENTHESES, component, componentPosition));
                                } else {
                                    throw new RuntimeException("Invalid token " + component + " in arithmetic expression at Line " + position.getLine() + ", Position " + position.getPosition());
                                }

                                currentPosition += component.length();
                            }

                            System.out.println("Current Char: " + input.charAt(counter));

                        } else if(input.charAt(counter) == '=') {
                            System.out.println("Found a variable: " + value.toString());
                            //Tokenize variable name
                            System.out.println("Adding a variable name " + value.toString() + " token at " + variablePosition);
                            tokens.add(new Token(Token.Type.VARIABLE, value.toString(), variablePosition));

                            // Tokenize assignment operator
                            System.out.println("Adding an assignment operator at " + position);
                            tokens.add(new Token(Token.Type.ASSIGNMENT, "=", position));
                            counter++;
                            position.setPosition(position.getPosition() + 1);
                        }
                    }

                    continue;
                }


                // If none of the above conditions match, it's an invalid token
                if (input.charAt(counter) != '\n' && input.charAt(counter) != ' ' && input.charAt(counter) != '#') {
                    throw new RuntimeException("Invalid token " + input.charAt(counter) + " at Line " + position.getLine() + ", Position " + position.getPosition());
                }
                
            }
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return tokens;
    }

    // Helper methods for lexer

    private void skipWhiteSpace() {
        while (counter < input.length() && Character.isWhitespace(input.charAt(counter))) {
            position.setPosition(position.getPosition() + 1);
            counter++;
        }
    }

    private int findIndentLevel(int startIndex) {
        
        int i = startIndex;
        int indentLevel = 0;
        int spaceCount = 0;

        // Check for tabs
        while (i < input.length() && input.charAt(i) == '\t') {
            indentLevel++;
            i++;
        }

        if (indentLevel > 0) {
            return indentLevel;
        }

        // Check for spaces
        while (i < input.length() && input.charAt(i) == ' ') {
            spaceCount++;
            i++;
        }

        if (spaceCount % 4 == 0) {
            return spaceCount / 4;
        } else if (spaceCount > 0 && spaceCount % 4 != 0) {
            throw new RuntimeException("Indentation with space should be 4 lines at Line " + position.getLine() + ", Position " + position.getPosition());
        }

        return 0;
    }

    private int[] moveToNextLine(int startIndex, int line, int position) {
        int i = startIndex;
        
        while (i < input.length() && input.charAt(i) != '\n') {
            i++;
        }
        
        if (i < input.length() && input.charAt(i) == '\n') {
            line++;
            position = 1;
        }

        return new int[]{(i + 1), line, position};
    }

    private static boolean isValidVariableName(String variableName) {
        // Check if the variable name is not empty
        if (variableName.isEmpty()) {
            return false;
        }

        // Check if the first character is a letter
        if (!Character.isLetter(variableName.charAt(0))) {
            return false;
        }

        // Check if the rest of the characters are alphanumeric or underscore
        for (int i = 1; i < variableName.length(); i++) {
            char ch = variableName.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '_') {
                return false;
            }
        }

        // If all checks pass, the variable name is valid
        return true;
    }

}