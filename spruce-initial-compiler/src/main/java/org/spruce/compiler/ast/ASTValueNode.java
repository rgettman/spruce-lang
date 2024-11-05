package org.spruce.compiler.ast;

import org.spruce.compiler.scanner.Location;

/**
 * An <code>ASTValueNode</code> is a leaf <code>ASTNode</code> with a value.
 */
public class ASTValueNode extends ASTNode {
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
     * Returns the value as a header value.
     * @return A header value for this node.
     */
    @Override
    public String getHeaderValue() {
        return getClass().getSimpleName() + "(" + getValue() + ") at " + getLocation();
    }
}