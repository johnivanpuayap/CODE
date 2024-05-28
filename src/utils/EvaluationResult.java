package src.utils;

public class EvaluationResult {
    private Type type;
    private String value;

    public EvaluationResult(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}