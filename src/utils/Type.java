package src.utils;

public enum Type {
    BEGIN_CODE, END_CODE, INDENT, DEDENT, NEWLINE, COMMA, EOF,
    INT, CHAR, FLOAT, BOOL, LITERAL,
    IDENTIFIER, ASSIGNMENT, ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO,
    LEFT_PARENTHESIS, RIGHT_PARENTHESIS,
    POSITIVE, NEGATIVE,
    GREATER, LESS, GREATER_EQUAL, LESS_EQUAL, NOT_EQUAL, EQUAL,
    AND, OR, NOT,
    IF, ELSE_IF, ELSE, BEGIN_IF, END_IF,
    SCAN, SCAN_VALUE,
    DISPLAY, DELIMITER, CONCATENATION, STRING_LITERAL, COLON, NEXT_LINE, SPECIAL_CHARACTER, ESCAPE_CODE_OPEN, EXPRESSION,
    ESCAPE_CODE_CLOSE,
    WHILE, BEGIN_WHILE, END_WHILE,
    FOR, BEGIN_FOR, END_FOR,
    CONTINUE, BREAK
}
