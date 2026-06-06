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
 * @param isShared Whether the current context is inside a shared context,
 *                 where instance variables are not available.
 */
public record ResolutionContext(Map<String, ParentSymbol> using,
                                ParentSymbol enclosingSymbol,
                                boolean isShared) {
    /**
     * Constructs a <code>ResolutionContext</code> with the given <code>Map</code>
     * of simple names to resolved symbols from use statements, the given
     * enclosing <code>ParentSymbol</code>, and NOT in a shared context.
     * @param using A <code>ParentSymbol</code> representing either a
     *              namespace or an enclosing type.
     * @param enclosingSymbol A <code>ParentSymbol</code> representing either a
     *                        namespace or an enclosing type.
     */
    public ResolutionContext(Map<String, ParentSymbol> using,
                             ParentSymbol enclosingSymbol) {
        this(using, enclosingSymbol, false);
    }

    /**
     * Create a new <code>ResolutionContext</code> based on this one, except it
     * has the given <code>ParentSymbol</code> as the enclosing type, and
     * copying whether it's in a shared context.
     * @param enclosing A <code>ParentSymbol</code> representing the new
     *                  enclosing type.
     * @return A new <code>ResolutionContext</code>.
     */
    public ResolutionContext withEnclosingSymbol(ParentSymbol enclosing) {
        return new ResolutionContext(using, enclosing, isShared);
    }

    /**
     * Create a new <code>ResolutionContext</code> based on this one, except it
     * has the given <code>ParentSymbol</code> as the enclosing type.
     * @param enclosing A <code>ParentSymbol</code> representing the new
     *                  enclosing type.
     * @param isShared Whether the current context is inside a shared context,
     *                 where instance variables are not available.
     * @return A new <code>ResolutionContext</code>.
     */
    public ResolutionContext withEnclosingSymbol(ParentSymbol enclosing, boolean isShared) {
        return new ResolutionContext(using, enclosing, isShared);
    }
}
