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

Increment 5

- [ ] ConditionalExpression
- [ ] Operations in Display
- [ ] Improve Error Handling
- [x] Tabs and Spaces for Indention
- [ ] WHILE Loops
- [ ] Test Case 1
- [ ] Test Case 2
- [ ] Test Case 3
- [ ] Test Case 4
- [ ] Test Case 5
- [ ] Test Case 6
- [ ] Test Case 7
- [ ] Test Case 8
- [ ] Test Case 9
- [ ] Test Case 10
- [ ] Test Case 11
- [ ] Test Case 12

Extra

- [ ] Improve Parser
- [ ] Improve Semantics Analyzer
- [ ] Functions
