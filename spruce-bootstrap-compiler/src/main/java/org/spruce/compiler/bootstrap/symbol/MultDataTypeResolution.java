package org.spruce.compiler.bootstrap.symbol;

import java.util.Optional;

/**
 * A <code>MultDataTypeResolution</code> is anything that needs to be resolved
 * to multiple datatypes.  It is meant to be implemented by AST classes that
 * refer to multiple declared symbols, e.g., use mult declarations and
 * intersection types.
 */
public interface MultDataTypeResolution {
    /**
     * Adds the resolved symbol.  Used during the resolution phase.  The symbol
     * can be looked up by name later.
     * @param symbol A <code>TypeSymbol</code> (or subtype).
     */
    void addResolvedDataType(TypeSymbol symbol);

    /**
     * Returns the resolved symbol by name, if it exists.  Used during the
     * analysis phase.  It is expected that <code>addResolvedDataType</code>
     * has already been called.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    Optional<TypeSymbol> getResolvedDataType(String name);
}
