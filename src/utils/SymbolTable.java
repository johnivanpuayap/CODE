package src.utils;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    public boolean insert(Symbol symbol) {
        if (symbols.containsKey(symbol.name)) {
            return false;
        }

        symbols.put(symbol.name, symbol);
        return true;
    }

    public Symbol lookup(String name) {
        return symbols.get(name);
    }

    public HashMap<String, Symbol> getSymbols() {
        return new HashMap<>(symbols);
    }

    public SymbolTable copy() {
        SymbolTable newTable = new SymbolTable();
        for (Map.Entry<String, Symbol> entry : symbols.entrySet()) {
            String newKey = new String(entry.getKey()); // Deep copy key if necessary, assuming keys are strings
            Symbol newValue = entry.getValue().copy(); // Deep copy value
            newTable.symbols.put(newKey, newValue);
        }
        return newTable;
    }
}