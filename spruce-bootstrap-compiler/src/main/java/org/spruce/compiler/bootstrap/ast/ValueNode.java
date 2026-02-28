package org.spruce.compiler.bootstrap.ast;

/**
 * A <code>ValueNode</code> has a value.
 */
public interface ValueNode extends Node {
    /**
     * Returns the value as a string.
     * @return The value as a string.
     */
    String getValue();
}
