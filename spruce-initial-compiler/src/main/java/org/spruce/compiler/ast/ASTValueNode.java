package org.spruce.compiler.ast;

import org.spruce.compiler.scanner.Location;

/**
 * An <code>ASTValueNode</code> is a leaf <code>ASTNode</code> with a value.
 */
public /*abstract*/ class ASTValueNode extends ASTNode {
    private final String myValue;

    /**
     * Constructs an <code>ASTValueNode</code> with the given <code>Location</code>
     * and the string value from the <code>Token</code>.
     * @param location The <code>Location</code>.
     * @param value The string value.
     */
    public ASTValueNode(Location location, String value) {
        super(location);
        myValue = value;
    }

    /**
     * Returns the string value.
     * @return The string value.
     */
    public String getValue() {
        return myValue;
    }

    /**
     * Prints this node to the output stream.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     */
    @Override
    protected void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + toString());
    }

    /**
     * Returns a string of the format "ClassSimpleName(value) at Location".
     * @return A string representation of this node.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getValue() + ") at " + getLocation();
    }
}