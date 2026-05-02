package org.spruce.compiler.bootstrap.symbol;

/**
 * A <code>ChildSymbolTable</code> is a <code>SymbolTable</code> that has a
 * <code>ParentSymbol</code>.
 */
public class ChildSymbolTable extends SymbolTable {
    private final ParentSymbol myParent;

    /**
     * Constructs a <code>SymbolTable</code> with the given <code>Scope</code>
     * and with the given parent <code>SymbolTable</code>.
     * @param scope A <code>Scope</code>.
     * @param parent The <code>ParentSymbol</code>.
     */
    public ChildSymbolTable(SymbolTable.Scope scope, ParentSymbol parent) {
        super(scope);
        myParent = parent;
    }

    /**
     * Returns the <code>ParentSymbol</code>.
     * @return A <code>ParentSymbol</code>.
     */
    public ParentSymbol getParent() {
        return myParent;
    }
}
