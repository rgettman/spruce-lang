package org.spruce.compiler.bootstrap.ast;

import java.util.Optional;

import org.spruce.compiler.bootstrap.symbol.Symbol;

/**
 * A <code>MultSymbolReference</code> is anything that needs to be resolved to
 * multiple symbols.  It is meant to be implemented by AST classes that refer
 * to multiple declared symbols, e.g., use mult declarations.
 * @param <S> The type or subtype of resolved <code>Symbol</code>.
 */
public interface MultSymbolReference<S extends Symbol> {
    /**
     * Adds the resolved symbol.  Used during the analysis phase.  The symbol
     * can be looked up by name later.
     * @param symbol A <code>Symbol</code> (or subtype).
     */
    void addResolvedSymbol(S symbol);

    /**
     * Returns the resolved symbol by name, if it exists.  Used during the code
     * generation phase.  It is expected that <code>addResolvedSymbol</code>
     * has already been called.
     * @return An <code>Optional&lt;S&gt;</code> (Symbol or subtype).
     */
    Optional<S> getResolvedSymbol(String name);
}
