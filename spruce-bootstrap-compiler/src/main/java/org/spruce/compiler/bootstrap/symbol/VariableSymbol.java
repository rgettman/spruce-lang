package org.spruce.compiler.bootstrap.symbol;

import org.spruce.compiler.bootstrap.common.Location;

/**
 * A <code>VariableSymbol</code> is a <code>Symbol</code> representing a
 * variable, field, or parameter that has been declared.  It is an
 * <code>EntitySymbol</code> with an associated datatype symbol.
 */
public class VariableSymbol extends Symbol implements EntitySymbol {
    private TypeSymbol myDataType;

    /**
     * Constructs a <code>VariableSymbol</code> at the given <code>Location</code>,
     * with the given name, and what <code>SymbolTable</code> this belongs to.
     * @param loc The <code>Location</code> of this <code>Symbol</code>.
     * @param name The name of this symbol.
     * @param kind the <code>Kind</code> of this <code>Symbol</code>.
     * @param parent The parent <code>SymbolTable</code>.
     * @param flags All flags belonging to this symbol.
     */
    public VariableSymbol(Location loc, String name, Kind kind, SymbolTable parent, long flags) {
        super(loc, name, kind, parent, flags);
    }

    @Override
    public TypeSymbol getDataType() {
        return myDataType;
    }

    @Override
    public void setDataType(TypeSymbol dataType) {
        myDataType = dataType;
    }
}
