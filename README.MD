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
- [X] Comments
    - [X] Comments Ignore Indentation
- [ ] Variable Declaration and Initialization
    - [ ] Check if the Variable Name is Valid
    - [X] INT
        - [X] Check if variable type is valid
    - [X] FLOAT
    - [X] CHAR
    - [X] BOOLEAN
- [X] Reserved words
    - [X] Check for variable names that are only similar in uppercase
- [X] DISPLAY function


Increment 2
- [X] concatenation (&)
- [X] next line ($)
- [X] escape code ([])
- [ ] SCAN function
- [ ] arithmetic operation
    - [ ] Addition
    - [ ] Subtraction

