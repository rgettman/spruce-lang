package org.spruce.compiler.bootstrap.symbol;

import java.util.List;

import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;

import org.spruce.compiler.bootstrap.resolution.Resolver;
import org.spruce.compiler.bootstrap.symbol.Symbol.Kind;

/**
 * A <code>BasicSymbolCreator</code> provides basic symbol creation functionality.
 * Subclasses represent symbol creators of various categories of AST elements and
 * can obtain references to each other for creating symbols for AST elements
 * outside their category, using the <code>SymbolCreator</code> class.
 */
public class BasicSymbolCreator {
    private final SymbolCreator mySymbolCreator;
    private final MessageProducer myMsgProducer;
    private final GlobalLookup myGlobalLookup;

    /**
     * Constructs a <code>BasicSymbolCreator</code> using a
     * <code>MessageProducer</code>, referring to a <code>SymbolCreator</code>.
     *
     * @param symbolCreator A <code>SymbolCreator</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param globalLookup A <code>GlobalLookup</code>.
     */
    public BasicSymbolCreator(SymbolCreator symbolCreator, MessageProducer msgProducer, GlobalLookup globalLookup) {
        mySymbolCreator = symbolCreator;
        myMsgProducer = msgProducer;
        myGlobalLookup = globalLookup;
    }

    /**
     * Returns the <code>ClassesSymbolCreator</code>.
     * @return The <code>ClassesSymbolCreator</code>.
     */
    public ClassesSymbolCreator getClassesSymbolCreator() {
        return mySymbolCreator.getClassesSymbolCreator();
    }

    /**
     * Returns the <code>StatementsSymbolCreator</code>.
     * @return The <code>StatementsSymbolCreator</code>.
     */
    public StatementsSymbolCreator getStatementsSymbolCreator() {
        return mySymbolCreator.getStatementsSymbolCreator();
    }

    /**
     * Returns the <code>TopLevelSymbolCreator</code>.
     * @return The <code>TopLevelSymbolCreator</code>.
     */
    public TopLevelSymbolCreator getTopLevelSymbolCreator() {
        return mySymbolCreator.getTopLevelSymbolCreator();
    }

    /**
     * Returns the <code>TypesSymbolCreator</code>.
     * @return The <code>TypesSymbolCreator</code>.
     */
    public TypesSymbolCreator getTypesSymbolCreator() {
        return mySymbolCreator.getTypesSymbolCreator();
    }

    /**
     * Creates a <code>CompilerMessage</code> of type <code>ERROR</code> at the
     * given <code>Location</code> with the given message, and adds it to the
     * internal list of compiler messages.
     * @param loc The <code>Location</code>.
     * @param msg The message.
     */
    public void error(Location loc, String msg) {
        myMsgProducer.error(loc, msg);
    }

    /**
     * Creates a <code>CompilerMessage</code> of type <code>NOTE</code> at the
     * given <code>Location</code> with the given message, and adds it to the
     * internal list of compiler messages.
     * @param loc The <code>Location</code>.
     * @param msg The message.
     */
    public void note(Location loc, String msg) {
        myMsgProducer.note(loc, msg);
    }

    /**
     * Inserts the given <code>Symbol</code> into the given <code>SymbolTable</code>,
     * if the symbol's name doesn't already exist.  If it does already exist,
     * produces an error message instead.  This shouldn't be called to insert a
     * namespace symbol; it's expected for different compilation units to
     * declare the same namespace.
     * @param table The <code>SymbolTable</code>.
     * @param symbol The <code>Symbol</code>.
     */
    public void insertSymbol(SymbolTable table, Symbol symbol) {
        String name = symbol.getName();
        SymbolTable current = table;
        boolean keepChecking = true;
        while (keepChecking) {
            if (current.containsSymbolName(name)) {
                Symbol original = current.get(name);
                handleSameSymbolName(symbol, original);
                keepChecking = false;
            }
            else if (current.getScope() == SymbolTable.Scope.SCOPE && current instanceof ChildSymbolTable child) {
                // Can't declare same-name symbols in a SCOPE, up through the first
                // non-SCOPE symbol table.  Check the parent symbol table.
                current = child.getParent().getParent();
            }
            else {
                table.insertSymbol(symbol);
                keepChecking = false;
            }
        }
    }

    /**
     * Inserts the method symbol into a map of method name to list of symbols.
     * @param table The <code>SymbolTable</code>.
     * @param methodName The method name.
     * @param symbol The <code>ParameterizedSymbol</code> for the method.
     */
    public void insertMethod(SymbolTable table, String methodName, ParameterizedSymbol symbol) {
        table.insertMethod(symbol, methodName);
    }

    // These symbol kind combinations can exist at the same scope level, so
    // symbols of the same name with one of these combinations is a conflict
    // error:
    // - Namespace and Type
    // - Type and Namespace
    // - Type and Type
    // - Type and Field
    // - Field and Type
    // - Method and Method
    // - Local var/parameter and Local var/parameter
    private void handleSameSymbolName(Symbol symbol, Symbol original) {
        if (symbol.getKind() == Kind.NAMESPACE) {
            // Handled specially in TopLevelSymbolCreator.  Shouldn't get here.
            throw internalError("Creating namespace symbol " + symbol.getName() +
                    " unexpected here!");
        }
        else if (symbol.isType()) {
            if (original.getKind() == Kind.NAMESPACE || original.isType() ||
                    original.getKind() == Kind.FIELD) {
                handleNameConflictError(symbol, original);
                return;
            }
        }
        else if (symbol.getKind() == Kind.FIELD) {
            if (original.isType() || original.getKind() == Kind.FIELD) {
                handleNameConflictError(symbol, original);
                return;
            }
        }
        else if (symbol.getKind() == Kind.METHOD && original.getKind() == Kind.METHOD) {
            // Method symbol names (includes signatures) only conflict with
            // other methods.
            error(symbol.getLocation(), "Duplicate method found: " + symbol.getName());
            note(original.getLocation(), "Originally declared here.");
            return;
        }
        else if (symbol.isLocal() && original.isLocal()) {
            // Parameters and local variables only conflict with themselves.
            error(symbol.getLocation(), "Duplicate identifier found: " + symbol.getName());
            note(original.getLocation(), "Originally declared here.");
            return;
        }
        // Shadowed and obscured symbols are never at the same scope level,
        // so those situations are still legal.
        // But if we get here, somehow an illegal combinations of kinds with
        // the same name has occurred at the same scope level, but the symbol
        // table cannot hold same-named symbols.
        throw internalError("Unexpected conflict on name " + symbol.getName() +
                ", kinds " + symbol.getKind() + " and " + original.getKind() + "!");
    }

    /**
     * Used when two symbol names conflict with each other.
     * Symbol names are already determined to be the same at this point.
     * @param symbol The newly created <code>Symbol</code>.
     * @param original An already existing <code>Symbol</code>.
     */
    protected void handleNameConflictError(Symbol symbol, Symbol original) {
        String name = symbol.getName();
        String originalKind = original.getKind().toString().toLowerCase();
        String symbolKind = symbol.getKind().toString().toLowerCase();

        error(original.getLocation(), originalKind + " " + name + " conflicts with " +
                symbolKind + " of the same name.");
        error(symbol.getLocation(), symbolKind + " " + name + " conflicts with " +
                originalKind + " of the same name.");
    }

    /**
     * Returns the <code>MessageProducer</code>.
     * @return The <code>MessageProducer</code>.
     */
    protected MessageProducer getMessageProducer() {
        return myMsgProducer;
    }

    /**
     * Returns the <code>List</code> of <code>CompilerMessage</code>s.
     * @return The <code>List</code> of <code>CompilerMessage</code>s.
     */
    public List<CompilerMessage> getCompilerMessages() {
        return myMsgProducer.getCompilerMessages();
    }

    /**
     * Returns the <code>GlobalLookup</code>.
     * @return The <code>GlobalLookup</code>.
     */
    public GlobalLookup getGlobalLookup() {
        return myGlobalLookup;
    }

    /**
     * Returns an early <code>Resolver</code> so use statements and formal
     * parameter types can be resolved early, here, in the symbol creation
     * phase.
     * @return A <code>Resolver</code>.
     */
    public Resolver getEarlyResolver() {
        return mySymbolCreator.getEarlyResolver();
    }

    /**
     * A helper method that creates and returns, but does not throw, an
     * <code>IllegalStateException</code> with the given error message.
     * @param expected The error message.
     * @return An <code>IllegalStateException</code>.
     */
    protected IllegalStateException internalError(String expected) {
        return new IllegalStateException("Internal error: Expected " + expected + "!");
    }
}

