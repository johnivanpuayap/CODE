package src.utils;

public class Position {
    private int line;
    private int column;

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getColumn() {
        return column;
    }

    public void add(int value) {
        column += value;
    }

    public void newLine() {
        line++;
        column = 1;
    }

    @Override
    public String toString() {
        return " at Line: " + line + ", Column: " + column;
    }
}

