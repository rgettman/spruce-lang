package org.spruce.compiler.symbol;

import org.spruce.compiler.common.Location;

/**
 * A <code>Symbol</code> is a name that has been declared in a compilation
 * unit.  It keeps a reference back to its parent <code>SymbolTable</code> and
 * it has its own child <code>SymbolTable</code>.
 */
public class Symbol {
    private final Location myLocation;
    private final String myName;
    private final SymbolTable myParent;
    private final long myFlags;

    /**
     * Constructs a <code>Symbol</code> at the given <code>Location</code>,
     * with the given name, what <code>SymbolTable</code> this belongs to, and
     * a child <code>SymbolTable</code>.
     * @param name The name of this symbol.
     * @param parent The parent <code>SymbolTable</code>.
     * @param flags All flags belonging to this symbol.
     */
    public Symbol(Location loc, String name, SymbolTable parent, long flags) {
        myLocation = loc;
        myName = name;
        myParent = parent;
        myFlags = flags;
    }

    /**
     * Returns the <code>Location</code>.
     * @return The <code>Location</code>.
     */
    public Location getLocation() {
        return myLocation;
    }

    /**
     * Returns the name of this symbol.
     * @return The name of this symbol.
     */
    public String getName() {
        return myName;
    }

    /**
     * Returns the parent <code>SymbolTable</code>.
     * @return The parent <code>SymbolTable</code>.
     */
    public SymbolTable getParent() {
        return myParent;
    }

    /**
     * Returns the flags.
     * @return The flags.
     */
    public long getFlags() {
        return myFlags;
    }

    /**
     * Returns the String representation of this symbol.
     * @return The String representation of this symbol.
     */
    @Override
    public String toString() {
        return toString("", true);
    }

    /**
     * Helper method to create a string representation of this symbol.  It takes
     * into account where in the tree this symbol is.
     * @param prefix A string to indent the printing of this symbol.
     * @param isTail Whether this node is last in its siblings (or the only child).
     * @return The String representation of this symbol.
     */
    public String toString(String prefix, boolean isTail) {
        return prefix + (isTail ? "└── " : "├── ") + "\"" + getName() + "\"(" + getFlags() + ") at "
                + getLocation() + "\n";
    }
}
