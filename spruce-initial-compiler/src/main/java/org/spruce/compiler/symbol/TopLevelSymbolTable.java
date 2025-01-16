package org.spruce.compiler.symbol;

import java.util.Optional;

/**
 * A <code>TopLevelSymbolTable</code> is a <code>SymbolTable</code> that
 * contains a possible <code>namespace</code> declaration and possible
 * <code>use</code> declarations.
 */
public class TopLevelSymbolTable extends SymbolTable {
    private ChildSymbolTable myNamespaceTable;

    /**
     * Constructs a <code>TopLevelSymbolTable</code> with the <code>TOP</code>
     * scope.
     */
    public TopLevelSymbolTable() {
        super(Scope.TOP);
    }

    /**
     * Adds a namespace <code>ChildSymbolTable</code>.  Check if there is
     * already a namespace table before adding another one.
     * @param namespaceTable A <code>ChildSymbolTable</code> representing the
     *                       namespace.
     * @see #getNamespace()
     */
    public void addNamespace(ChildSymbolTable namespaceTable) {
        myNamespaceTable = namespaceTable;
    }

    /**
     * Returns whether a namespace symbol table has been added.
     * @return Whether a namespace symbol table has been added.
     */
    public Optional<ChildSymbolTable> getNamespace() {
        return Optional.ofNullable(myNamespaceTable);
    }

    /**
     * Helper method to create a string representation of this symbol table.  It takes
     * into account where in the tree this table is.
     * @param prefix A string to indent the printing of this symbol table.
     * @param isTail Whether this node is last in its siblings (or the only child).
     * @return The String representation of this symbol table.
     */
    @Override
    public String toString(String prefix, boolean isTail) {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString(prefix, isTail));
        if (myNamespaceTable != null) {
            buf.append(myNamespaceTable.toString(prefix + (isTail ? "    " : "|   "), true));
        }
        return buf.toString();
    }
}
