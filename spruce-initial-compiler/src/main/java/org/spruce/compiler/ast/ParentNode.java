package org.spruce.compiler.ast;

import java.util.List;

/**
 * A <code>ParentNode</code> has children.
 */
public interface ParentNode extends Node {
    /**
     * Returns a <code>List</code> of <code>Node</code>s.
     * @return A <code>List</code> of <code>Node</code>s.
     */
    List<Node> getChildren();
}
