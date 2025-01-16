package org.spruce.compiler.semantic;

import java.util.List;

import org.spruce.compiler.common.MessageProducer;
import org.spruce.compiler.common.CompilerMessage;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.symbol.Symbol;
import org.spruce.compiler.symbol.SymbolTable;

/**
 * A <code>BasicAnalyzer</code> provides basic semantic analyzer functionality.
 * Subclasses represent analyzers of various categories of AST elements and can
 * obtain references to each other for analyzing AST elements outside their
 * category, using the SemanticAnalyzer class.
 */
public class BasicAnalyzer {
    private final SemanticAnalyzer myAnalyzer;
    private final MessageProducer myMsgProducer;

    /**
     * Constructs a <code>BasicAnalyzer</code> using a <code>MessageProducer</code>.
     *
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public BasicAnalyzer(SemanticAnalyzer analyzer, MessageProducer msgProducer) {
        myAnalyzer = analyzer;
        myMsgProducer = msgProducer;
    }

    /**
     * Returns the <code>NamesAnalyzer</code>.
     * @return The <code>NamesAnalyzer</code>.
     */
    public NamesAnalyzer getNamesAnalyzer() {
        return myAnalyzer.getNamesAnalyzer();
    }

    /**
     * Returns the <code>TopLevelAnalyzer</code>.
     * @return The <code>TopLevelAnalyzer</code>.
     */
    public TopLevelAnalyzer getTopLevelAnalyzer() {
        return myAnalyzer.getTopLevelAnalyzer();
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
     * Inserts the given <code>Symbol</code> into the given <code>SymbolTable</code>,
     * if the symbol's name doesn't already exist.  If it does already exist,
     * produces an error message instead.
     * @param table The <code>SymbolTable</code>.
     * @param symbol The <code>Symbol</code>.
     */
    public void insertSymbol(SymbolTable table, Symbol symbol) {
        String name = symbol.getName();
        if (table.containsSymbolName(name)) {
            error(symbol.getLocation(), "Duplicate identifier found: " + name);
        }
        else {
            table.insertSymbol(symbol);
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
