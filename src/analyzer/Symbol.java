package src.analyzer;

import src.utils.Type;

// Represents a symbol in the symbol table
class Symbol {
    Type type;
    String name;
    String value;
    boolean initialized; // Flag to track if the symbol is initialized

    public Symbol(Type type, String name) {
        this.name = name;
        this.type = type;
        this.initialized = false; // Initialize as not initialized
    }

    public Symbol(Type type, String name, String value) {
        this(type, name);
        this.value = value;
        this.initialized = true; // Initialize as initialized
    }

    public void setValue(String value) {
        this.value = value;
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}