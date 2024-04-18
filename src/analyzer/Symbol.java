package src.analyzer;

// Represents a symbol in the symbol table
class Symbol {
    String name;
    String type;
    Object value; // Value of the symbol
    boolean initialized; // Flag to track if the symbol is initialized

    public Symbol(String name, String type) {
        this.name = name;
        this.type = type;
        this.initialized = false; // Initialize as not initialized
    }

    public void setValue(Object value) {
        this.value = value;
        this.initialized = true; // Mark as initialized when assigning a value
    }

    public boolean isInitialized() {
        return initialized;
    }
}