package org.spruce.compiler.symbol;

/**
 * A <code>ChildSymbolTable</code> is a <code>SymbolTable</code> that has a
 * parent <code>SymbolTable</code>.
 */
public class ChildSymbolTable extends SymbolTable {
    private final SymbolTable myParent;

    /**
     * Constructs a <code>SymbolTable</code> with the given <code>Scope</code>
     * and with the given parent <code>SymbolTable</code>.
     * @param scope A <code>Scope</code>.
     * @param parent The parent <code>SymbolTable</code>.
     */
    public ChildSymbolTable(SymbolTable.Scope scope, SymbolTable parent) {
        super(scope);
        myParent = parent;
    }

    /**
     * Returns the parent <code>SymbolTable</code>.
     * @return A <code>SymbolTable</code>.
     */
    public SymbolTable getParent() {
        return myParent;
    }
}
