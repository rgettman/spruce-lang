package org.spruce.compiler.bootstrap.symbol;

import java.util.Optional;

import org.spruce.compiler.bootstrap.common.Location;

/**
 * A <code>TypeSymbol</code> is a <code>ParentSymbol</code> that has its own
 * reference to a supertype.
 */
public class TypeSymbol extends ParentSymbol {
    /**
     * The "symbol" for a void method return type.
     */
    public static final TypeSymbol VOID = new TypeSymbol(
            new Location("<void>", 0, 0, "void"),
            "void", Kind.VOID, new SymbolTable(SymbolTable.Scope.GLOBAL), FLAG_NONE
    );
    private TypeSymbol mySupertype;

    /**
     * Constructs a <code>TypeSymbol</code> at the given <code>Location</code>,
     * with the given name, what <code>SymbolTable</code> this belongs to, and
     * a child <code>SymbolTable</code>.
     * @param loc The <code>Location</code>.
     * @param name The name of this symbol.
     * @param kind The <code>Type</code> of this symbol.
     * @param parent The parent <code>SymbolTable</code>.
     * @param flags All flags belonging to this symbol.
     */
    public TypeSymbol(Location loc, String name, Kind kind, SymbolTable parent, long flags) {
        super(loc, name, kind, parent, flags);
    }

    /**
     * Returns the supertype <code>TypeSymbol</code>, if it exists.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    public Optional<TypeSymbol> getSupertype() {
        return Optional.ofNullable(mySupertype);
    }

    /**
     * Sets the supertype <code>TypeSymbol</code>.  There is no supertype
     * symbol if this method is never called.
     * @param supertype A <code>TypeSymbol</code>.
     */
    public void setSupertype(TypeSymbol supertype) {
        mySupertype = supertype;
    }
}
