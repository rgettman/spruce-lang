package org.spruce.compiler.bootstrap.symbol;

/**
 * A <code>SymbolDeclaration</code> is anything that declares a symbol.  It is
 * meant to be implemented by AST classes that declare symbols, e.g. class
 * declarations and local variable declarations.
 * @param <S> The type or subtype of declared <code>Symbol</code>.
 */
public interface SymbolDeclaration<S extends Symbol> {
    /**
     * Sets the declaration symbol.  Used during the symbol creation phase.
     * @param symbol A <code>Symbol</code> (or subtype).
     */
    void setDeclSymbol(S symbol);

    /**
     * Returns the declaration symbol.  Used during the semantic analysis
     * phase.  It is expected that <code>setDeclSymbol</code> has already been
     * called.
     * @return A <code>Symbol</code> (or subtype).
     */
    S getDeclSymbol();
}
