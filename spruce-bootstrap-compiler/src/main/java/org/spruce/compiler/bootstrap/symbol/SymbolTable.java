package org.spruce.compiler.bootstrap.symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spruce.compiler.bootstrap.common.Location;

import static org.spruce.compiler.bootstrap.symbol.Symbol.FLAG_NONE;
import static org.spruce.compiler.bootstrap.symbol.Symbol.Kind.*;

/**
 * A <code>SymbolTable</code> represents declarations found in a parsed node.
 * It is organized hierarchically by containing a map of symbol names to
 * <code>Symbol</code>s, which may have their own child symbol table(s).
 */
public class SymbolTable {
    /**
     * The scope of a symbol table.
     */
    public enum Scope {
        GLOBAL,
        MEMBER,
        NAMESPACE,
        SCOPE,
        TOP, TYPE
    }

    private final Scope myScope;
    private final Map<String, Symbol> myTable;
    private final Map<String, List<Symbol>> myMethods;

    /**
     * Constructs a <code>SymbolTable</code> with the given <code>Scope</code>
     * and with the given parent <code>SymbolTable</code>.
     * @param scope A <code>Scope</code>.
     */
    public SymbolTable(Scope scope) {
        myScope = scope;
        myTable = new HashMap<>();
        myMethods = new HashMap<>();
    }

    /**
     * Constructs a root <code>SymbolTable</code> with a <code>Scope</code> of
     * <code>TOP</code> and no parent <code>SymbolTable</code>.
     */
    public SymbolTable() {
        this(Scope.TOP);
        ParentSymbol unnamedNamespace = new ParentSymbol(new Location("<unnamed>", 0, 0, "unavailable"),
                GlobalLookup.UNNAMED_NAMESPACE_NAME, Symbol.Kind.NAMESPACE, this, FLAG_NONE);
        unnamedNamespace.setTable(new ChildSymbolTable(Scope.NAMESPACE, unnamedNamespace));
        getTable().put(GlobalLookup.UNNAMED_NAMESPACE_NAME, unnamedNamespace);
    }

    /**
     * Returns the <code>Scope</code>.
     * @return The <code>Scope</code>.
     */
    public Scope getScope() {
        return myScope;
    }

    /**
     * Returns whether this table contains a symbol of the given name.
     * @param name The name of the symbol to find in this table.
     * @return Whether this table contains a symbol of the given name.
     */
    public boolean containsSymbolName(String name) {
        return myTable.containsKey(name);
    }

    /**
     * Inserts the given <code>Symbol</code> into this table.  The caller must
     * ensure that the symbol is not already present in this table.
     * @param symbol A <code>Symbol</code>.
     * @see #containsSymbolName(String)
     */
    public void insertSymbol(Symbol symbol) {
        myTable.put(symbol.getName(), symbol);
    }

    /**
     * Inserts the given <code>ParameterizedSymbol</code> into a mapping of
     * method name to a list of symbols.  The caller must call
     * {@link #insertSymbol(Symbol)} first.
     * @param symbol A <code>Symbol</code>.
     * @see #containsSymbolName(String)
     */
    public void insertMethod(ParameterizedSymbol symbol, String methodName) {
        List<Symbol> overloads;
        if (myMethods.containsKey(methodName)) {
            overloads = myMethods.get(methodName);
        }
        else {
            overloads = new ArrayList<>();
            myMethods.put(methodName, overloads);
        }
        overloads.add(symbol);
    }

    /**
     * Returns whether this symbol contains a method with the given name.
     * @param methodName The method name.
     * @return Whether this symbol contains a method with the given name.
     */
    public boolean containsMethodName(String methodName) {
        return myMethods.containsKey(methodName);
    }

    /**
     * Returns how many symbols exist in this table.
     * @return How many symbols exist in this table.
     */
    public int size() {
        return myTable.size();
    }

    /**
     * Returns whether this table directly contains a <code>Symbol</code> with
     * the given name and of the given <code>Kind</code>.
     * @param name The name of the <code>Symbol</code> to find.
     * @param kinds A <code>List</code> of <code>Kind</code>s the
     *              <code>Symbol</code> can be.
     * @return Whether this table directly contains a <code>Symbol</code> with
     *      the given name and of the given <code>Kind</code>.
     */
    private boolean containsName(String name, List<Symbol.Kind> kinds) {
        if (!myTable.containsKey(name)) {
            return false;
        }
        Symbol found = myTable.get(name);
        return kinds.contains(found.getKind());
    }

    /**
     * Returns whether this table directly contains a namespace
     * <code>Symbol</code> of the given name.
     * @param name The name of the <code>Symbol</code> to find.
     * @return Whether this table directly contains a <code>Symbol</code> with
     *      the given name and of the <code>Kind</code> <code>NAMESPACE</code>.
     */
    public boolean containsNamespace(String name) {
        return containsName(name, List.of(NAMESPACE));
    }

    /**
     * Returns whether this table directly contains a namespace or type
     * <code>Symbol</code> of the given name.
     * @param name The name of the <code>Symbol</code> to find.
     * @return Whether this table directly contains a <code>Symbol</code> with
     *      the given name and of the <code>Kind</code> <code>NAMESPACE</code>
     *      or one of "type" kinds.
     */
    public boolean containsNamespaceOrType(String name) {
        return containsName(name, List.of(NAMESPACE)) || containsType(name);
    }

    /**
     * Returns whether this table directly contains a type <code>Symbol</code>
     * of the given name.
     * @param name The name of the <code>Symbol</code> to find.
     * @return Whether this table directly contains a <code>Symbol</code> with
     *      the given name and of one of "type" kinds.
     */
    public boolean containsType(String name) {
        // Keep consistent with Symbol.Kind.isType()!
        return containsName(name, List.of(NAMESPACE, CLASS, INTERFACE));
    }

    /**
     * Returns whether this table directly contains a field <code>Symbol</code>
     * of the given name.
     * @param name The name of the <code>Symbol</code> to find.
     * @return Whether this table directly contains a <code>Symbol</code> with
     *      the given name and of the kind <code>FIELD</code>.
     */
    public boolean containsField(String name) {
        return containsName(name, List.of(FIELD));
    }

    /**
     * Returns whether this table directly contains a variable <code>Symbol</code>
     * of the given name.
     * @param name The name of the <code>Symbol</code> to find.
     * @return Whether this table directly contains a <code>Symbol</code> with
     *      the given name and of one of "variable" kinds: field, parameter, local.
     */
    public boolean containsVariable(String name) {
        return containsName(name, List.of(FIELD, PARAMETER, LOCAL));
    }

    /**
     * Returns whether this table directly contains a <code>Symbol</code> of
     * the given name that could be a namespace, type, or variable.
     * @param name The name of the <code>Symbol</code> to find.
     * @return Whether this table directly contains a <code>Symbol</code> with
     *      the given name and of one of "variable" kinds: field, parameter, local.
     */
    public boolean containsNamespaceTypeOrVariable(String name) {
        return containsNamespaceOrType(name) || containsVariable(name);
    }

    /**
     * Retrieves the <code>Symbol</code> according to the given name.
     * @param name The name.
     * @return A <code>Symbol</code>.
     */
    public Symbol get(String name) {
        return myTable.get(name);
    }

    /**
     * Returns the <code>Map</code> of symbol name to <code>Symbol</code>.
     * @return A <code>Map&ltString, Symbol&gt;</code>.
     */
    public Map<String, Symbol> getTable() {
        return myTable;
    }

    /**
     * Adds a <code>ParentSymbol</code>, or returns an existing
     * <code>ParentSymbol</code> that matches, if found as a direct child.
     * @param ifAbsent The <code>ParentSymbol</code>.
     * @return The found <code>ParentSymbol</code>, or <code>ifAbsent</code>
     *     if not found.
     */
    public ParentSymbol findOrAddSymbol(ParentSymbol ifAbsent) {
        String name = ifAbsent.getName();
        return (ParentSymbol) myTable.computeIfAbsent(name, _ -> ifAbsent);
    }

    /**
     * Returns the String representation of this symbol table.
     * @return The String representation of this symbol table.
     */
    @Override
    public String toString() {
        return toString("", true);
    }

    /**
     * Helper method to create a string representation of this symbol table.  It takes
     * into account where in the tree this table is.
     * @param prefix A string to indent the printing of this symbol table.
     * @param isTail Whether this node is last in its siblings (or the only child).
     * @return The String representation of this symbol table.
     */
    public String toString(String prefix, boolean isTail) {
        StringBuilder buf = new StringBuilder();
        String newPrefix = prefix + (isTail ? "    " : "|   ");
        buf.append(newPrefix).append(myScope.toString()).append(" - ").append(size()).append(" entries:\n");
        int i = 0;
        for (Map.Entry<String, Symbol> entry : myTable.entrySet()) {
            buf.append(entry.getValue().toString(newPrefix, (i == size() - 1)));
            i++;
        }
        return buf.toString();
    }
}
