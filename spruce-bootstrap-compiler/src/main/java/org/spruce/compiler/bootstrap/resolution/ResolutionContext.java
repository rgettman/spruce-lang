package org.spruce.compiler.bootstrap.resolution;

import java.util.Map;

import org.spruce.compiler.bootstrap.symbol.ParentSymbol;

/**
 * A <code>ResolutionContext</code> contains contextual data to aid in the
 * resolution of simple and qualified names.
 * @param using A <code>Map</code> of simple names to resolved symbols from use
 *              statements.
 * @param enclosingSymbol A <code>ParentSymbol</code> representing either a
 *                        namespace or an enclosing type.
 */
public record ResolutionContext(Map<String, ParentSymbol> using,
                                ParentSymbol enclosingSymbol) {
    /**
     * Create a new <code>ResolutionContext</code> based on this one, except it
     * has the given <code>ParentSymbol</code> as the enclosing type.
     * @param enclosing A <code>ParentSymbol</code> representing the new
     *                  enclosing type.
     * @return A new <code>ResolutionContext</code>.
     */
    public ResolutionContext withEnclosingSymbol(ParentSymbol enclosing) {
        return new ResolutionContext(using, enclosing);
    }
}
