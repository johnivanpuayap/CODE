package src.utils;

public class Position {
    private int line;
    private int position;

    public Position(int line, int position) {
        this.line = line;
        this.position = position;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Line: " + line + ", Position: " + position;
    }
}

