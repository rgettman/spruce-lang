package org.spruce.compiler.ast;

import org.spruce.compiler.scanner.Location;

/**
 * The top-level class for abstract syntax tree nodes.
 */
public abstract class ASTNode
{
    private Location myLocation;

    /**
     * Constructs an <code>ASTNode</code> with a <code>Location</code>.
     * @param location The <code>Location</code> of the node.
     */
    public ASTNode(Location location)
    {
        myLocation = location;
    }

    /**
     * Returns the <code>Location</code>.
     * @return The <code>Location</code>.
     */
    public Location getLocation()
    {
        return myLocation;
    }

    /**
     * Prints this node to the output stream.
     */
    public void print()
    {
        print("", true);
    }

    /**
     * Helper method to print this node.  Takes into account where in the tree
     * this node is.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     */
    protected abstract void print(String prefix, boolean isTail);
}
