package org.spruce.compiler.bootstrap.symbol;

/**
 * An <code>EntityResolution</code> is anything that needs to be resolved to a
 * variable, parameter, field, method, or constructor.  It is meant to be
 * implemented by AST classes that evaluate to a such a construct, e.g.,
 * expression names, field access, method invocations, and more.
 */
public interface EntityResolution {
    /**
     * Sets the resolved entity symbol.  Used during the resolution phase.
     * @param symbol A <code>EntitySymbol</code>.
     */
    void setResolvedEntity(EntitySymbol symbol);

    /**
     * Returns the resolved entity symbol.  Used during the code generation
     * phase. It is expected that <code>setResolvedSymbol</code> has already
     * been called.
     * @return A <code>EntitySymbol</code>.
     */
    EntitySymbol getResolvedEntity();
}
