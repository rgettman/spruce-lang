package org.spruce.compiler.ast.expressions;

import org.spruce.compiler.ast.ASTValueNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSuper</code> is the <code>super</code> keyword.</p>
 */
public class ASTSuper extends ASTValueNode {
    /**
     * Constructs an <code>ASTSuper</code> given the <code>Location</code>
     * and the string value of the token.
     * @param location The <code>Location</code>.
     * @param value The string value.
     */
    public ASTSuper(Location location, String value) {
        super(location, value);
    }
}
