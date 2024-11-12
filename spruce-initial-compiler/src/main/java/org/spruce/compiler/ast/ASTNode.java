package org.spruce.compiler.ast;

import org.spruce.compiler.scanner.Location;

/**
 * The top-level class for abstract syntax tree nodes.
 */
public abstract class ASTNode implements Node {
    private final Location myLocation;

    /**
     * Constructs an <code>ASTNode</code> with a <code>Location</code>.
     * @param location The <code>Location</code> of the node.
     */
    public ASTNode(Location location) {
        myLocation = location;
    }

    /**
     * Returns the <code>Location</code>.
     * @return The <code>Location</code>.
     */
    @Override
    public Location getLocation() {
        return myLocation;
    }

    /**
     * Returns the String representation of this node.
     * @return The String representation of this node.
     */
    @Override
    public String toString() {
        return toString("", true);
    }

    /**
     * Helper method to create a string representation of this node.  It takes
     * into account where in the tree this node is.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     * @return The String representation of this node.
     */
    public String toString(String prefix, boolean isTail) {
        return prefix + (isTail ? "└── " : "├── ") + getClass().getSimpleName() + "(" + getHeaderValue() + ") at "
                + getLocation() + "\n";
    }
}
