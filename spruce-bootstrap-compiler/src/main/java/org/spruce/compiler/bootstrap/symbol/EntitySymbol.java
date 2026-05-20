package org.spruce.compiler.bootstrap.symbol;

/**
 * An <code>EntitySymbol</code> is a symbol that has a reference to a
 * <code>TypeSymbol</code>.  It is meant to be implemented by symbols that
 * represent an entity with a datatype.
 */
public interface EntitySymbol {
    /**
     * Sets the datatype of this entity symbol, during symbol creation.
     * @param symbol A <code>TypeSymbol</code>.
     */
    void setDataType(TypeSymbol symbol);

    /**
     * Returns the datatype of this entity symbol, during symbol resolution.
     * @return A <code>TypeSymbol</code>.
     */
    TypeSymbol getDataType();
}
