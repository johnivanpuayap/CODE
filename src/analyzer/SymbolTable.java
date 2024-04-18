package src.analyzer;

import java.util.HashMap;
import java.util.Map;

class SymbolTable {
    private Map<String, Symbol> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    // Insert a symbol into the symbol table
    public boolean insert(Symbol symbol) {
        if (symbols.containsKey(symbol.name)) {
            return false;
        }
        
        symbols.put(symbol.name, symbol);
        return true;
    }

    // Lookup a symbol in the symbol table
    public Symbol lookup(String name) {
        return symbols.get(name);
    }
}