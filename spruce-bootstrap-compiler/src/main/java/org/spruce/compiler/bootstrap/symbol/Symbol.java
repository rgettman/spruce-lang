package org.spruce.compiler.bootstrap.symbol;

import org.spruce.compiler.bootstrap.common.Location;

/**
 * A <code>Symbol</code> represents a name that has been declared in a
 * compilation unit.  It keeps a reference back to its parent <code>SymbolTable</code>.
 */
public class Symbol {

    public static final String NAME_CONSTRUCTOR = "<init>";

    public static final long FLAG_NONE = 0L;

    public static final long FLAG_MOD_ABSTRACT = 0x10L;
    public static final long FLAG_MOD_FINAL = 0x40L;
    public static final long FLAG_MOD_OVERRIDE = 0x80L;
    public static final long FLAG_MOD_SHARED = 0x200L;

    /**
     * The kind of symbol.
     */
    public enum Kind {
        BLOCK,
        CLASS, CONSTRUCTOR,
        FIELD, FOR_STMT,
        IF_STMT, INTERFACE,
        LOCAL,
        METHOD,
        NAMESPACE,
        PARAMETER,
        VOID,
        WHILE_STMT;
    }

    private final Location myLocation;
    private final String myName;
    private final SymbolTable myParent;
    private final Kind myKind;
    private final long myFlags;

    /**
     * Constructs a <code>Symbol</code> at the given <code>Location</code>,
     * with the given name, and what <code>SymbolTable</code> this belongs to.
     * @param loc The <code>Location</code> of this <code>Symbol</code>.
     * @param name The name of this symbol.
     * @param kind the <code>Kind</code> of this <code>Symbol</code>.
     * @param parent The parent <code>SymbolTable</code>.
     * @param flags All flags belonging to this symbol.
     */
    public Symbol(Location loc, String name, Kind kind, SymbolTable parent, long flags) {
        myName = name;
        myParent = parent;
        myLocation = loc;
        myKind = kind;
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
     * Returns the <code>Kind</code> of this symbol.
     * @return The <code>Kind</code> of this symbol.
     */
    public Kind getKind() {
        return myKind;
    }

    /**
     * Returns whether the <code>Kind</code> is a type.
     * @return Whether the <code>Kind</code> is a type.
     */
    public boolean isType() {
        // There will be more types!
        return myKind == Kind.CLASS || myKind == Kind.INTERFACE;
    }

    /**
     * Returns whether the <code>Kind</code> is a local declaration.
     * @return Whether the <code>Kind</code> is a local declaration.
     */
    public boolean isLocal() {
        return myKind == Kind.LOCAL || myKind == Kind.PARAMETER;
    }

    /**
     * Returns whether the <code>Kind</code> is a variable.
     * @return Whether the <code>Kind</code> is a variable.
     */
    public boolean isVariable() {
        return isLocal() || myKind == Kind.FIELD;
    }

    /**
     * Returns whether this symbol is shared.
     * @return Whether this symbol is shared.
     */
    public boolean isShared() {
        return (myFlags & FLAG_MOD_SHARED) != 0;
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
        StringBuilder buf = new StringBuilder(prefix);
        if (isTail) {
            buf.append("└── ");
        }
        else {
            buf.append("├── ");
        }
        buf.append(" \"").append(getName()).append("\"(").append(getKind()).append(",")
                .append(String.format("0x%08X", getFlags())).append(") at ")
                .append(getLocation()).append("\n");
        return buf.toString();
    }
}
