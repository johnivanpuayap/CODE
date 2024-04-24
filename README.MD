# Readme

This is a simple interpreter written in Java. It interprets code written in a custom language.

## How to Run

1. Compile the Java files:

   ```sh
   javac javac App.java
   ```

2. Run the Interpreter class.

   ```sh
   java App <input file directory>
   ```

   E.G.

   ```sh
   java App programs/basic.code
   ```

   Note: file should end in .code

# How It Works

The interpreter works in several stages:

1. Lexing: The Lexer class in src/lexer/Lexer.java takes the input code and converts it into a series of tokens.

2. Parsing: The Parser class in src/parser/Parser.java takes the tokens and converts them into an abstract syntax tree (AST).

3. Semantic Analysis: The SemanticsAnalyzer class in src/analyzer/SemanticsAnalyzer.java checks the AST for semantic errors.

4. Interpretation: The Interpreter class interprets the AST and executes the code.

# Coding Consistency

- Parser, every function should return a node
- Peek() after function is the start of the next line
- Throw errors early, e.g. If ()

# TODO

Increment 1

- [x] Basic Program Structure (BEGIN CODE END CODE block)
  - [x] Check for Indentation
  - [x] Variable Declaration should only happen after begin code
  - [x] Executable Code must only happen after the declaration
  - [x] Only one Statement per line
- [x] Comments
  - [x] Comments Ignore Indentation
  - [ ] Comments Can Be Put Anywhere, Create a function to ignore comments
- [x] Variable Declaration and Initialization
  - [x] Check if the Variable Name is valid
  - [x] INT
    - [x] Check if variable type is valid
    - [ ] Check if the it only uses 4 bytes
  - [x] FLOAT
    - [ ] Check if variable type is valid
    - [ ] Check if the it only uses 4 bytes
  - [x] CHAR
  - [x] BOOLEAN
- [ ] Reserved words
  - [x] Check for variable names that are only similar in uppercase
  - [ ] Check for Usage of Reserved Words in Variable Declaration
  - [ ] Check for Usage of Reserved Words in Statements
- [ ] DISPLAY function
  - [ ] concatenation (&)
  - [x] next line ($)
  - [x] escape code
- [x] SCAN function

- [x] Arithmetic operation
  - [x] Operator Precendence
  - [x] Parentheses
  - [ ] Unary Operation
  - [ ] Modulo Operation
- [x] Assignment Statements
  - [x] Single Assignment
    - [x] Check if assignment is valid
    - [ ] Check if the all the variable is defined
    - [x] If assignee is variable, check if it is initialized
  - [x] Multiple Assignment like x=y=z or x=y=5
    - [x] Check if all the identifiers exist
    - [x] Check if the last one is initialized
    - [ ] Check if all the data types are the same as the last one
- [ ] Comparison Operators
  - [ ] Greater than >, Lesser than <
  - [ ] Greater than or Equal to >=, Less than or Equal to <=
  - [ ] Equal ==, Not Equal <>
- [ ] Logical Operators

  - [ ] AND
  - [ ] OR
  - [ ] NOT

- [x] Unary Operators

  - [x] - Negative
  - [x] - Positive
  - [ ] Multiple Unary Operators

- [ ] If-Else

  - [ ] Tokenize
  - [ ] Check Syntax

- [ ] If-Else With Else IF

- [ ] If-Else with Mutlitple Else If
- [ ] Check for Indention Errors

Increment 5

- [ ] LOOPS
