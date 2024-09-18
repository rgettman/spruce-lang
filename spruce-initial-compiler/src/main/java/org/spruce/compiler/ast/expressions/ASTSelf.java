package org.spruce.compiler.ast.expressions;

import org.spruce.compiler.ast.ASTValueNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSelf</code> is the <code>self</code> keyword.</p>
 */
public class ASTSelf extends ASTValueNode
{
    /**
     * Constructs an <code>ASTSelf</code> given the <code>Location</code>
     * and the string value of the token.
     * @param location The <code>Location</code>.
     * @param value The string value.
     */
    public ASTSelf(Location location, String value) {
        super(location, value);
    }
}
