package org.spruce.compiler.bootstrap.symbol;

import org.spruce.compiler.bootstrap.common.Location;

/**
 * A <code>ParentSymbol</code> is a <code>Symbol</code> that has its own
 * <code>ChildSymbolTable</code>.
 */
public class ParentSymbol extends Symbol {
    private ChildSymbolTable myTable;

    /**
     * Constructs a <code>ParentSymbol</code> at the given <code>Location</code>,
     * with the given name, what <code>SymbolTable</code> this belongs to, and
     * a child <code>SymbolTable</code>.
     * @param loc The <code>Location</code>.
     * @param name The name of this symbol.
     * @param kind The <code>Type</code> of this symbol.
     * @param parent The parent <code>SymbolTable</code>.
     * @param dataType The <code>DataType</code> of this <code>Symbol</code>.
     * @param flags All flags belonging to this symbol.
     */
    public ParentSymbol(Location loc, String name, Kind kind, SymbolTable parent, DataType dataType, long flags) {
        super(loc, name, kind, parent, dataType, flags);
    }

    /**
     * Returns the <code>ChildSymbolTable</code>.
     * @return The <code>ChildSymbolTable</code>.
     */
    public ChildSymbolTable getTable() {
        return myTable;
    }

    /**
     * Sets the <code>ChildSymbolTable</code>.
     * @param table The <code>ChildSymbolTable</code>.
     */
    public void setTable(ChildSymbolTable table) {
        myTable = table;
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
        return super.toString(prefix, isTail) + (myTable != null ? myTable.toString(prefix, isTail) : "");
    }
}
