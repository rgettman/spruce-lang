package org.spruce.compiler.bootstrap.resolution;

import java.util.Map;

import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.TypeLookup;

/**
 * A <code>ResolutionContext</code> contains contextual data to aid in the
 * resolution of simple and qualified names.
 * @param global The global <code>TypeLookup</code>.
 * @param enclosingSymbol A <code>ParentSymbol</code> representing either a
 *                        namespace or an enclosing type.
 * @param using A <code>Map</code> of simple names to resolved symbols from use
 *              statements.
 */
public record ResolutionContext(TypeLookup global,
                                ParentSymbol enclosingSymbol,
                                Map<String, ParentSymbol> using) {
}
