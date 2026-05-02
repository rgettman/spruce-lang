package org.spruce.compiler.bootstrap.ast;

import org.spruce.compiler.bootstrap.symbol.Symbol;

/**
 * A <code>SymbolReference</code> is anything that needs to be resolved to a
 * symbol.  It is meant to be implemented by AST classes that refer to declared
 * symbols, e.g. use declarations, data types, and expression names.
 * @param <S> The type or subtype of resolved <code>Symbol</code>.
 */
public interface SymbolReference<S extends Symbol> {
    /**
     * Sets the resolved symbol.  Used during the analysis phase.
     * @param symbol A <code>Symbol</code> (or subtype).
     */
    void setResolvedSymbol(S symbol);

    /**
     * Returns the resolved symbol.  Used during the code generation
     * phase.  It is expected that <code>setResolvedSymbol</code> has already
     * been called.
     * @return A <code>Symbol</code> (or subtype).
     */
    S getResolvedSymbol();
}
