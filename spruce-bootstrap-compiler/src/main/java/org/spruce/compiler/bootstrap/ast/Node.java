package org.spruce.compiler.bootstrap.ast;

import org.spruce.compiler.bootstrap.common.Location;

/**
 * The top-level interface for abstract syntax tree nodes.
 */
public interface Node {
    /**
     * Returns the <code>Location</code>.
     * @return The <code>Location</code>.
     */
    Location getLocation();

    /**
     * Helper method to create a string representation of this node.  It takes
     * into account where in the tree this node is.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     * @return The String representation of this node.
     */
    String toString(String prefix, boolean isTail);

    /**
     * The header value is included in the first line of the string
     * representation of this node in the format specified by
     * @link #toString(prefix, isTail).
     * @return A header value for this node.
     */
    String getHeaderValue();
}
