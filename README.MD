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

We will use the same symbol table that we use on semantic analysis in the interpreter.

4. Interpretation: The Interpreter class interprets the AST and executes the code.

# Coding Consistency

- In parser, every function should return a node
- Peek() after every statement should be the start of the next line, use check for new line to ensure that there is only one statement per line
- Throw the syntax, semantics and runtime errors early in the function
- Do not throw an exception, print the error and exit the code. (But for easier debugging, you chose to throw exception)

# TODO

Final Project Presentation

- [ ] Conditional Expression
- [ ] Operations in Display
- [ ] Improve Error Handling
- [x] Tabs and Spaces for Indention
- [x] WHILE Loops
- [x] Test Case 1
- [x] Test Case 2
- [x] Test Case 3
- [x] Test Case 4
- [x] Test Case 5
- [x] Test Case 6
- [x] Test Case 7
- [x] Test Case 8
- [ ] Test Case 9
- [ ] Test Case 10
- [ ] Test Case 11
- [ ] Test Case 12
- [x] Check Display to put an error when accessing an array that was not initialized
- [ ] Fix error when there are blank lines between code
- [x] Better error handling for conditional statements
- [x] Improved Logical and Arithmetic Examples
- [ ] Improve Parser
- [ ] Improve Semantics Analyzer

# Additional Features

- [x] For Loops
- [x] BREAK
- [x] CONTINUE

Note: Since it was stated in the program specifications that all variable declaration is found after BEGIN CODE, our for loop should not declare a new variable, the format is like this

```plaintext
BEGIN CODE
   INT counter
   FOR (counter = 0; counter < 10; counter = counter + 1)
   BEGIN FOR
      DISPLAY: counter & "+"
   END FOR
   DISPLAY: $ & "Loop finished!"
END CODE
```

If u don't want to initialize a variable, it can be blank

```plaintext
BEGIN CODE
   INT counter = 0
   FOR (; counter < 10; counter = counter + 1)
   BEGIN FOR
      DISPLAY: counter & "+"
   END FOR
   DISPLAY: $ & "Loop finished!"
END CODE
```
