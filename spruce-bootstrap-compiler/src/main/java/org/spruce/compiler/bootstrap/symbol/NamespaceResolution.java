package org.spruce.compiler.bootstrap.symbol;

/**
 * A <code>NamespaceResolution</code> is anything that needs to be resolved to a
 * namespace.  It is meant to be implemented by AST classes that evaluate to a
 * namespace, e.g. use-all declarations.
 */
public interface NamespaceResolution {
    /**
     * Sets the resolved namespace symbol.  Used during the resolution phase.
     * @param symbol A <code>ParentSymbol</code> representing a namespace.
     */
    void setResolvedNamespace(ParentSymbol symbol);

    /**
     * Returns the resolved namespace symbol.  Used during the code generation
     * phase. It is expected that <code>setResolvedSymbol</code> has already
     * been called.
     * @return A <code>ParentSymbol</code> representing a namespace.
     */
    ParentSymbol getResolvedNamespace();
}
