package org.spruce.compiler.symbol;

import org.spruce.compiler.common.Location;

/**
 * A <code>Symbol</code> represents a name that has been declared in a
 * compilation unit.  It keeps a reference back to its parent <code>SymbolTable</code>.
 */
public class Symbol {
    public static final String NAME_CONSTRUCTOR = "<init>";
    public static final String NAME_SHARED_CONSTRUCTOR = "<clinit>";

    public static final long FLAG_NONE = 0L;

    public static final long FLAG_ACCESS_PRIVATE = 0x1L;
    public static final long FLAG_ACCESS_PROTECTED = 0x2L;
    public static final long FLAG_ACCESS_INTERNAL = 0x4L;
    public static final long FLAG_ACCESS_PUBLIC = 0x8L;

    public static final long FLAG_MOD_ABSTRACT = 0x10L;
    public static final long FLAG_MOD_DEFAULT = 0x20L;
    public static final long FLAG_MOD_FINAL = 0x40L;
    public static final long FLAG_MOD_OVERRIDE = 0x80L;
    public static final long FLAG_MOD_SEALED = 0x100L;
    public static final long FLAG_MOD_SHARED = 0x200L;
    public static final long FLAG_MOD_VOLATILE = 0x400L;

    public static final long FLAG_VARIABLE_MUT = 0x1000L;
    public static final long FLAG_VARIABLE_VAR = 0x2000L;

    public static final long FLAG_METHOD_MUT = 0x8000L;

    public static final long DEFAULT_ACCESS_CLASS = FLAG_ACCESS_INTERNAL;
    public static final long DEFAULT_ACCESS_CONSTRUCTOR = FLAG_ACCESS_PUBLIC;
    public static final long DEFAULT_ACCESS_FIELD = FLAG_ACCESS_PRIVATE;
    public static final long DEFAULT_ACCESS_METHOD = FLAG_ACCESS_PUBLIC;

    /**
     * The type of symbol.
     */
    public enum Type {
        ADT, ANNOTATION, ANNOTATION_TYPE_ELEMENT,
        BLOCK,
        CATCH, CLASS, CONSTRUCTOR,
        DO_STMT,
        ENUM, ENUM_CONSTANT,
        FIELD, FINALLY, FOR_STMT,
        IF_STMT, INTERFACE,
        LAMBDA, LOCAL,
        METHOD,
        NAMESPACE,
        PARAMETER,
        PATTERN,
        RECORD, RECORD_COMPONENT,
        SHARED_CONSTRUCTOR,
        TRY_STMT, TYPE_PARAMETER,
        USE,
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
