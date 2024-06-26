# Readme

This is a simple interpreter written in Java. It interprets code written in a custom language.

## How to Run

1. Compile the Java files: 

    ```sh
    javac javac Interpreter.java
    ```

2. Run the Interpreter class.
    
    ```sh
    java Interpreter <input file directory>
    ```

    E.G.
    
    ```sh
    java Interpreter programs/basic.code
    ```
    Note: file should end in .code

# How It Works

The interpreter works in several stages:

1. Lexing: The Lexer class in src/lexer/Lexer.java takes the input code and converts it into a series of tokens.

2. Parsing: The Parser class in src/parser/Parser.java takes the tokens and converts them into an abstract syntax tree (AST).

3. Semantic Analysis: The SemanticsAnalyzer class in src/analyzer/SemanticsAnalyzer.java checks the AST for semantic errors.

4. Interpretation: The Interpreter class interprets the AST and executes the code.

# TODO

Increment 1
- [X] Basic Program Structure (BEGIN CODE END CODE block)
    - [X] Check for Indentation
    - [X] Variable Declaration should only happen after begin code
    - [X] Executable Code must only happen after the declaration
    - [X] Only one Statement per line
- [X] Comments
    - [X] Comments Ignore Indentation
    - [ ] Comments Can Be Put Anywhere, Create a function to ignore comments
- [X] Variable Declaration and Initialization
    - [X] Check if the Variable Name is valid
    - [X] INT
        - [X] Check if variable type is valid
        - [ ] Check if the it only uses 4 bytes
    - [X] FLOAT
        - [ ] Check if variable type is valid
        - [ ] Check if the it only uses 4 bytes
    - [X] CHAR
    - [X] BOOLEAN
- [X] Reserved words
    - [X] Check for variable names that are only similar in uppercase
- [ ] DISPLAY function


Increment 2
- [X] concatenation (&)
- [X] next line ($)
- [X] escape code
- [X] SCAN function
- [X] arithmetic operation
    - [X] Operator Precendence
    - [X] Parentheses
- [X] Assignment Statements
    - [X] Single Assignment
        - [X] Check if assignment is valid
        - [X] If assignee is variable, check if it exists
    - [X] Multiple Assignment like x=y=z or x=y=5
        - [X] Check if all the data types are the same
        - [X] Check if all the identifiers exist
        - [X] Check if the last one is initialized


Increment 3
- [ ] Logical Operators
    - [ ] AND
    - [ ] OR
    - [ ] NOT
- [X] Unary Operators
    - [X] - Negative
    - [X] + Positive

Increment 4
- [ ] IF-ELSE Selection
- [ ] IF-ELSE with multiple alternatives

Increment 5
- [ ] LOOPS

Final Presentation
- [ ] Fix Semantics Analyzer
- [ ] Improve Error Handling