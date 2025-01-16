package org.spruce.compiler.symbol;

import java.util.HashMap;
import java.util.Map;

/**
 * A <code>SymbolTable</code> represents declarations found in a parsed node.
 * It is organized hierarchically by containing a map of symbol names to
 * <code>Symbol</code>s, which may have their own child symbol table(s).
 */
public class SymbolTable {
    /**
     * The scope of a symbol table.
     */
    public enum Scope {
        CONSTRUCTOR,
        FOR_EXPR, FOR_STMT,
        IF_STMT,
        METHOD,
        NAMESPACE,
        SCOPE,
        TOP, TYPE,
        USE,
        WHILE_STMT
    }

    private final Scope myScope;
    private final Map<String, Symbol> myTable;

    /**
     * Constructs a <code>SymbolTable</code> with the given <code>Scope</code>
     * and with the given parent <code>SymbolTable</code>.
     * @param scope A <code>Scope</code>.
     */
    public SymbolTable(Scope scope) {
        myScope = scope;
        myTable = new HashMap<>();
    }

    /**
     * Constructs a root <code>SymbolTable</code> with a <code>Scope</code> of
     * <code>TOP</code> and no parent <code>SymbolTable</code>.
     */
    public SymbolTable() {
        this(Scope.TOP);
    }

    /**
     * Returns the <code>Scope</code>.
     * @return The <code>Scope</code>.
     */
    public Scope getScope() {
        return myScope;
    }

    /**
     * Returns whether this table contains a symbol of the given name.
     * @param name The name of the symbol to find in this table.
     * @return Whether this table contains a symbol of the given name.
     */
    public boolean containsSymbolName(String name) {
        return myTable.containsKey(name);
    }

    /**
     * Inserts the given <code>Symbol</code> into this table.  The caller must
     * ensure that the symbol is not present in this table.
     * @param symbol A <code>Symbol</code>.
     * @see #containsSymbolName(String)
     */
    public void insertSymbol(Symbol symbol) {
        myTable.put(symbol.getName(), symbol);
    }

    /**
     * Returns how many symbols exist in this table.
     * @return How many symbols exist in this table.
     */
    public int size() {
        return myTable.size();
    }

    /**
     * Retrieves the <code>Symbol</code> according to the given name.
     * @param name The name.
     * @return A <code>Symbol</code>.
     */
    public Symbol get(String name) {
        return myTable.get(name);
    }

    /**
     * Returns the <code>Map</code> of symbol name to <code>Symbol</code>.
     * @return A <code>Map&ltString, Symbol&gt;</code>.
     */
    public Map<String, Symbol> getTable() {
        return myTable;
    }

    /**
     * Returns the String representation of this symbol table.
     * @return The String representation of this symbol table.
     */
    @Override
    public String toString() {
        return toString("", true);
    }

    /**
     * Helper method to create a string representation of this symbol table.  It takes
     * into account where in the tree this table is.
     * @param prefix A string to indent the printing of this symbol table.
     * @param isTail Whether this node is last in its siblings (or the only child).
     * @return The String representation of this symbol table.
     */
    public String toString(String prefix, boolean isTail) {
        StringBuilder buf = new StringBuilder();
        String newPrefix = prefix + (isTail ? "    " : "|   ");
        buf.append(newPrefix).append(myScope.toString()).append(" - ").append(size()).append(" entries:\n");
        int i = 0;
        for (Map.Entry<String, Symbol> entry : myTable.entrySet()) {
            buf.append(entry.getValue().toString(newPrefix, (i == size() - 1)));
            i++;
        }
        return buf.toString();
    }
}
