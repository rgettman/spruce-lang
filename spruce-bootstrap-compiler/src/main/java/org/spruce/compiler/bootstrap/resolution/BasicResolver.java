package org.spruce.compiler.bootstrap.resolution;

import java.util.List;

import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.TypeLookup;

/**
 * A <code>BasicResolver</code> provides basic symbol resolution functionality.
 * Subclasses represent resolvers of various categories of AST elements and
 * can obtain references to each other for resolving data types and expression
 * types outside their category, using the <code>Resolver</code> class.
 */
public class BasicResolver {
    private final Resolver myResolver;
    private final MessageProducer myMsgProducer;
    private final TypeLookup myGlobalLookup;

    /**
     * Constructs a <code>BasicResolver</code> using a
     * <code>MessageProducer</code>, referring to an <code>Resolver</code>.
     *
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The global <code>TypeLookup</code>.
     */
    public BasicResolver(Resolver resolver, MessageProducer msgProducer, TypeLookup global) {
        myResolver = resolver;
        myMsgProducer = msgProducer;
        myGlobalLookup = global;
    }

    /**
     * Returns the <code>TopLevelResolver</code>.
     * @return The <code>TopLevelResolver</code>.
     */
    public TopLevelResolver getTopLevelResolver() {
        return myResolver.getTopLevelResolver();
    }

    /**
     * Returns the <code>ClassesResolver</code>.
     * @return The <code>ClassesResolver</code>.
     */
    public ClassesResolver getClassesResolver() {
        return myResolver.getClassesResolver();
    }

    /**
     * Returns the <code>StatementsResolver</code>.
     * @return The <code>StatementsResolver</code>.
     */
    public StatementsResolver getStatementsResolver() {
        return myResolver.getStatementsResolver();
    }
    /**
     * Returns the <code>ExpressionsResolver</code>.
     * @return The <code>ExpressionsResolver</code>.
     */
    public ExpressionsResolver getExpressionsResolver() {
        return myResolver.getExpressionsResolver();
    }

    /**
     * Returns the <code>TypesResolver</code>.
     * @return The <code>TypesResolver</code>.
     */
    public TypesResolver getTypesResolver() {
        return myResolver.getTypesResolver();
    }

    /**
     * Returns the global <code>TypeLookup</code>.
     * @return The global <code>TypeLookup</code>.
     */
    public TypeLookup getGlobalLookup() {
        return myGlobalLookup;
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
