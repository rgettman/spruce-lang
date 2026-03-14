package org.spruce.compiler.bootstrap.symbol;

import org.spruce.compiler.bootstrap.common.Location;

/**
 * A <code>Symbol</code> represents a name that has been declared in a
 * compilation unit.  It keeps a reference back to its parent <code>SymbolTable</code>.
 */
public class Symbol {
    public static final String NAME_CONSTRUCTOR = "<init>";

    public static final long FLAG_NONE = 0L;

    public static final long FLAG_MOD_OVERRIDE = 0x80L;
    public static final long FLAG_MOD_SHARED = 0x200L;

    /**
     * The type of symbol.
     */
    public enum Type {
        BLOCK,
        CLASS, CONSTRUCTOR,
        FIELD, FOR_STMT,
        IF_STMT,
        LOCAL,
        METHOD,
        NAMESPACE,
        PARAMETER,
        WHILE_STMT
    }

    private final Location myLocation;
    private final String myName;
    private final SymbolTable myParent;
    private final Type myType;
    private final long myFlags;

    /**
     * Constructs a <code>Symbol</code> at the given <code>Location</code>,
     * with the given name, what <code>SymbolTable</code> this belongs to, and
     * a child <code>SymbolTable</code>.
     * @param name The name of this symbol.
     * @param parent The parent <code>SymbolTable</code>.
     * @param flags All flags belonging to this symbol.
     */
    public Symbol(Location loc, String name, Type type, SymbolTable parent, long flags) {
        myName = name;
        myParent = parent;
        myLocation = loc;
        myType = type;
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
     * Returns the <code>Type</code> of this symbol.
     * @return The <code>Type</code> of this symbol.
     */
    public Type getType() {
        return myType;
    }

    /**
     * Returns the flags.
     * @return The flags.
     */
    public long getFlags() {
        return myFlags;
    }

    /**
     * Returns the String representation.
     * @return The String representation.
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
        return prefix + (isTail ? "└── " : "├── ") + "\"" + getName() + "\"(" + getType() + "," +
                String.format("0x%08X", getFlags()) + ") at " + getLocation() + "\n";
    }
}
