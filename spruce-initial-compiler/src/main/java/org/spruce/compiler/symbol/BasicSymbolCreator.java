package org.spruce.compiler.symbol;

import java.util.List;

import org.spruce.compiler.common.CompilerMessage;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.common.MessageProducer;

/**
 * A <code>BasicSymbolCreator</code> provides basic symbol creation functionality.
 * Subclasses represent symbol creators of various categories of AST elements and
 * can obtain references to each other for creating symbols for AST elements
 * outside their category, using the <code>SymbolCreator</code> class.
 */
public class BasicSymbolCreator {
    private final SymbolCreator mySymbolCreator;
    private final MessageProducer myMsgProducer;

    /**
     * Constructs a <code>BasicSymbolCreator</code> using a
     * <code>MessageProducer</code>, referring to a <code>SymbolCreator</code>.
     *
     * @param symbolCreator A <code>SymbolCreator</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public BasicSymbolCreator(SymbolCreator symbolCreator, MessageProducer msgProducer) {
        mySymbolCreator = symbolCreator;
        myMsgProducer = msgProducer;
    }

    /**
     * Returns the <code>ClassesSymbolCreator</code>.
     * @return The <code>ClassesSymbolCreator</code>.
     */
    public ClassesSymbolCreator getClassesSymbolCreator() {
        return mySymbolCreator.getClassesSymbolCreator();
    }

    /**
     * Returns the <code>ExpressionsSymbolCreator</code>.
     * @return The <code>ExpressionsSymbolCreator</code>.
     */
    public ExpressionsSymbolCreator getExpressionsSymbolCreator() {
        return mySymbolCreator.getExpressionsSymbolCreator();
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
     * produces an error message instead.
     * @param table The <code>SymbolTable</code>.
     * @param symbol The <code>Symbol</code>.
     */
    public void insertSymbol(SymbolTable table, Symbol symbol) {
        String name = symbol.getName();
        SymbolTable current = table;
        boolean keepChecking = true;
        while (keepChecking) {
            if (current.containsSymbolName(name)) {
                error(symbol.getLocation(), "Duplicate identifier found: " + name);
                Symbol original = current.get(name);
                note(original.getLocation(), "Originally declared here.");
                keepChecking = false;
            }
            else if (current.getScope() == SymbolTable.Scope.SCOPE && current instanceof ChildSymbolTable child) {
                // Can't declare same-name symbols in a SCOPE, up through the first
                // non-SCOPE symbol table.  Check the parent symbol table.
                current = child.getParent();
            }
            else {
                table.insertSymbol(symbol);
                keepChecking = false;
            }
        }
    }

    /**
     * Returns the <code>List</code> of <code>CompilerMessage</code>s.
     * @return The <code>List</code> of <code>CompilerMessage</code>s.
     */
    public List<CompilerMessage> getCompilerMessages() {
        return myMsgProducer.getCompilerMessages();
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

