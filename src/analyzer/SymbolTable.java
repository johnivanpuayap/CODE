package src.analyzer;

import java.util.HashMap;
import java.util.Map;

class SymbolTable {
    private Map<String, Symbol> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    // Insert a symbol into the symbol table
    public void insert(Symbol symbol) throws Exception {
        if (symbols.containsKey(symbol.name)) {
            throw new Exception("Symbol '" + symbol.name + "' already declared in this scope");
        }
        symbols.put(symbol.name, symbol);
    }

    // Lookup a symbol in the symbol table
    public Symbol lookup(String name) {
        return symbols.get(name);
    }
}