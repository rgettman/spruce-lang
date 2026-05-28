package org.spruce.compiler.bootstrap.symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.common.Location;

/**
 * A <code>TypeSymbol</code> is a <code>ParentSymbol</code> that may have its
 * own reference to a superclass and/or references to superinterfaces.
 */
public class TypeSymbol extends ParentSymbol {
    /**
     * The "symbol" for a void method return type.
     */
    public static final TypeSymbol VOID = new TypeSymbol(
            new Location("<void>", 0, 0, "void"),
            "void", Kind.VOID, new SymbolTable(SymbolTable.Scope.GLOBAL), FLAG_NONE
    );

    private TypeSymbol mySuperclass;
    private final List<TypeSymbol> mySuperinterfaces;

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
        // Don't take up a ton of memory allocating mostly empty arrays for all
        // types.
        mySuperinterfaces = new ArrayList<>(2);
    }

    /**
     * Returns the superclass <code>TypeSymbol</code>, if it exists.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    public Optional<TypeSymbol> getSuperclass() {
        return Optional.ofNullable(mySuperclass);
    }

    /**
     * Sets the superclass <code>TypeSymbol</code>.  There is no supertype
     * symbol if this method is never called.
     * @param superclass A <code>TypeSymbol</code>.
     */
    public void setSuperclass(TypeSymbol superclass) {
        mySuperclass = superclass;
    }

    /**
     * Returns a possibly empty list of all the superinterface
     * <code>TypeSymbol</code>s.
     * @return An <code>List&lt;TypeSymbol&gt;</code>.
     */
    public List<TypeSymbol> getSuperinterfaces() {
        return mySuperinterfaces;
    }

    /**
     * Adds a superinterface <code>TypeSymbol</code>.
     * @param superinterface A <code>TypeSymbol</code>.
     */
    public void addSuperinterface(TypeSymbol superinterface) {
        mySuperinterfaces.add(superinterface);
    }
}
