package src.utils;

public class Symbol {
    Type type;
    String name;
    String value;
    boolean initialized = false;

    public Symbol(Type type, String name) {
        this.name = name;
        this.type = type;
    }

    public Symbol(Type type, String name, String value) {
        this(type, name);
        this.value = value;

        if (value != null) {
            this.initialized = true;
        }
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    // Copy constructor or a copy method for deep copying
    public Symbol copy() {
        return new Symbol(this.type, this.name, this.value);
    }
}