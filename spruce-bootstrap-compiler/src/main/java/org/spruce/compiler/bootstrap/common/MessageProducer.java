package org.spruce.compiler.bootstrap.common;

import java.util.List;

/**
 * A <code>MessageProducer</code> produces <code>CompilerMessage</code>s and
 * stores them for later retrieval.
 */
public interface MessageProducer {
    /**
     * Creates a <code>CompilerMessage</code> of type <code>ERROR</code> at the
     * given <code>Location</code> with the given message, and stores it for
     * later retrieval.
     * @param loc The <code>Location</code>.
     * @param msg The message.
     */
    void error(Location loc, String msg);

    /**
     * Creates a <code>CompilerMessage</code> of type <code>WARNING</code> at the
     * given <code>Location</code> with the given message, and stores it for
     * later retrieval.
     * @param loc The <code>Location</code>.
     * @param msg The message.
     */
    void warning(Location loc, String msg);

    /**
     * Creates a <code>CompilerMessage</code> of type <code>NOTE</code> at the
     * given <code>Location</code> with the given message, and stores it for
     * later retrieval.
     * @param loc The <code>Location</code>.
     * @param msg The message.
     */
    void note(Location loc, String msg);

    /**
     * Returns the <code>List</code> of <code>CompilerMessage</code>s.
     * @return The <code>List</code> of <code>CompilerMessage</code>s.
     */
    List<CompilerMessage> getCompilerMessages();

    /**
     * Adds the given <code>CompilerMessage</code> to the internal list of
     * compiler messages.
     * @param cm The <code>CompilerMessage</code>.
     */
    void addCompilerMessage(CompilerMessage cm);
}
