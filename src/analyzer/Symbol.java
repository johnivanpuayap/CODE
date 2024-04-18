package src.analyzer;

// Represents a symbol in the symbol table
class Symbol {
    String identifier;
    String type;
    String value;
    boolean initialized; // Flag to track if the symbol is initialized

    public Symbol(String type, String identifier) {
        this.identifier = identifier;
        this.type = type;
        this.initialized = false; // Initialize as not initialized
    }

    public Symbol(String type, String identifier, String value) {
        this(type, identifier);
        this.value = value;
        this.initialized = true; // Initialize as initialized
    }


    public boolean isInitialized() {
        return initialized;
    }
}