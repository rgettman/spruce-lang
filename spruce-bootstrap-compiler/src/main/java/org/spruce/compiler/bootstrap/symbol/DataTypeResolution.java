package org.spruce.compiler.bootstrap.symbol;

/**
 * A <code>DataTypeResolution</code> is anything that needs to be resolved to a
 * datatype.  It is meant to be implemented by AST classes that evaluate to a
 * datatype, e.g. use declarations, data types, declared variables, method
 * invocations, expressions, and more.
 */
public interface DataTypeResolution {
    /**
     * Sets the resolved datatype symbol.  Used during the resolution phase.
     * @param symbol A <code>TypeSymbol</code>.
     */
    void setResolvedDataType(TypeSymbol symbol);

    /**
     * Returns the resolved datatype symbol.  Used during the code generation
     * phase. It is expected that <code>setResolvedDataType</code> has already
     * been called.
     * @return A <code>TypeSymbol</code>.
     */
    TypeSymbol getResolvedDataType();
}
