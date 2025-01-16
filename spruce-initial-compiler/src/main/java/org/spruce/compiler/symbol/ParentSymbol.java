package org.spruce.compiler.symbol;

import org.spruce.compiler.common.Location;

/**
 * A <code>ParentSymbol</code> is a <code>Symbol</code> that has its own
 * <code>ChildSymbolTable</code>.
 */
public class ParentSymbol extends Symbol {
    private final ChildSymbolTable myTable;

    /**
     * Constructs a <code>Symbol</code> at the given <code>Location</code>,
     * with the given name, what <code>SymbolTable</code> this belongs to, and
     * a child <code>SymbolTable</code>.
     * @param loc The <code>Location</code>.
     * @param name The name of this symbol.
     * @param parent The parent <code>SymbolTable</code>.
     * @param flags All flags belonging to this symbol.
     * @param scope The <code>Scope</code> of this symbol.
     */
    public ParentSymbol(Location loc, String name, SymbolTable parent, long flags, SymbolTable.Scope scope) {
        super(loc, name, parent, flags);
        myTable = new ChildSymbolTable(scope, getParent());
    }

    /**
     * Returns the <code>ChildSymbolTable</code>.
     * @return The <code>ChildSymbolTable</code>.
     */
    public ChildSymbolTable getTable() {
        return myTable;
    }

    /**
     * Helper method to create a string representation of this symbol.  It takes
     * into account where in the tree this node is.
     * @param prefix A string to indent the printing of this symbol.
     * @param isTail Whether this node is last in its siblings (or the only child).
     * @return The String representation of this symbol.
     */
    @Override
    public String toString(String prefix, boolean isTail) {
        return super.toString(prefix, isTail) + myTable.toString(prefix, isTail);
    }
}
