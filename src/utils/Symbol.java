package src.utils;


public class Symbol {
    Type type;
    String name;
    String value;
    boolean initialized; 

    public Symbol(Type type, String name) {
        this.name = name;
        this.type = type;
        this.initialized = false;
    }

    public Symbol(Type type, String name, String value) {
        this(type, name);
        this.value = value;
        this.initialized = true;
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
}