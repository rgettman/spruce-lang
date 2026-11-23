package org.spruce.compiler.bootstrap.analysis;

import java.util.List;

import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;

public class BasicAnalyzer {
    private final Analyzer myAnalyzer;
    private final MessageProducer myMsgProducer;

    /**
     * Constructs a <code>BasicAnalyzer</code> using a <code>MessageProducer</code>.
     * @param analyzer An <code>Analyzer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public BasicAnalyzer(Analyzer analyzer, MessageProducer msgProducer) {
        myAnalyzer = analyzer;
        myMsgProducer = msgProducer;
    }

    /**
     * Returns the <code>TopLevelAnalyzer</code>.
     * @return The <code>TopLevelAnalyzer</code>.
     */
    public TopLevelAnalyzer getTopLevelAnalyzer() {
        return myAnalyzer.getTopLevelAnalyzer();
    }

    /**
     * Returns the <code>ClassesAnalyzer</code>.
     * @return The <code>ClassesAnalyzer</code>.
     */
    public ClassesAnalyzer getClassesAnalyzer() {
        return myAnalyzer.getClassesAnalyzer();
    }

    /**
     * Returns the <code>StatementsAnalyzer</code>.
     * @return The <code>StatementsAnalyzer</code>.
     */
    public StatementsAnalyzer getStatementsAnalyzer() {
        return myAnalyzer.getStatementsAnalyzer();
    }

    /**
     * Returns the <code>ExpressionsAnalyzer</code>.
     * @return The <code>ExpressionsAnalyzer</code>.
     */
    public ExpressionsAnalyzer getExpressionsAnalyzer() {
        return myAnalyzer.getExpressionsAnalyzer();
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
     * Creates a <code>CompilerMessage</code> of type <code>WARNING</code> at the
     * given <code>Location</code> with the given message, and adds it to the
     * internal list of compiler messages.
     * @param loc The <code>Location</code>.
     * @param msg The message.
     */
    public void warn(Location loc, String msg) {
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
     * Returns the <code>List</code> of <code>CompilerMessage</code>s.
     * @return The <code>List</code> of <code>CompilerMessage</code>s.
     */
    public List<CompilerMessage> getCompilerMessages() {
        return myMsgProducer.getCompilerMessages();
    }

    /**
     * A helper method that creates and returns, but does not throw, an
     * <code>IllegalStateException</code> with the given error message.
     * @param message The error message.
     * @return An <code>IllegalStateException</code>.
     */
    protected IllegalStateException internalError(String message) {
        return new IllegalStateException("Internal error: " + message);
    }
}
