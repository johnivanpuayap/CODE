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

    public SymbolTable copy() {
        SymbolTable newTable = new SymbolTable();
        newTable.symbols = new HashMap<>(symbols);
        return newTable;
    }
}