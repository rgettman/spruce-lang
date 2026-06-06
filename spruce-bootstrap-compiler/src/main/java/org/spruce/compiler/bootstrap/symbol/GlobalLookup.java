package org.spruce.compiler.bootstrap.symbol;

import java.util.Map;
import java.util.Optional;

import org.spruce.compiler.bootstrap.common.Location;

import static org.spruce.compiler.bootstrap.symbol.Symbol.FLAG_NONE;

/**
 * A <code>GlobalLookup</code> is the master lookup for all declared
 * namespaces and types within those namespaces.  It is a <code>SymbolTable</code>
 * of scope <code>GLOBAL</code>.  Only top-level namespaces are
 * directly inserted here, e.g. "org" or "spruce", not "spruce.collections".
 */
public class GlobalLookup extends SymbolTable {
    /**
     * The "name" of the unnamed namespace.  This is used when no namespace
     * declaration is found on a compilation unit.
     */
    public static final String UNNAMED_NAMESPACE_NAME = "";

    /**
     * Constructs a <code>GlobalLookup</code> with one built-in namespace,
     * the unnamed namespace.
     */
    public GlobalLookup() {
        super(Scope.GLOBAL);
        ParentSymbol unnamedNamespace = new ParentSymbol(new Location("<unnamed>", 0, 0, "unavailable"),
                UNNAMED_NAMESPACE_NAME, Symbol.Kind.NAMESPACE, this, FLAG_NONE);
        unnamedNamespace.setTable(new ChildSymbolTable(Scope.NAMESPACE, unnamedNamespace));
        getTable().put(UNNAMED_NAMESPACE_NAME, unnamedNamespace);
    }

    /**
     * Returns the namespace <code>ParentSymbol</code> by name, if it exists.
     * @param name The name of the namespace <code>ParentSymbol</code> to retrieve.
     * @return An <code>Optional&lt;ParentSymbol&gt;</code>.
     */
    public Optional<ParentSymbol> getNamespace(String name) {
        Map<String, Symbol> table = getTable();
        if (table.containsKey(name)) {
            return Optional.ofNullable((ParentSymbol) table.get(name));
        }
        else {
            return Optional.empty();
        }
    }
}
