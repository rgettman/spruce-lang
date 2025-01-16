package org.spruce.compiler.symbol;

import org.spruce.compiler.common.Location;

/**
 * Not sure if this will be used yet.
 */
public class LocalVarDeclSymbol extends Symbol {
    public LocalVarDeclSymbol(Location loc, String name, SymbolTable parent, long flags) {
        super(loc, name, parent, flags);
    }
}
