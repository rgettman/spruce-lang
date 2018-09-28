package org.spruce.compiler.ast;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTThis</code> is the <code>this</code> keyword.</p>
 */
public class ASTThis extends ASTValueNode
{
    /**
     * Constructs an <code>ASTThis</code> given the <code>Location</code>
     * and the string value of the token.
     * @param location The <code>Location</code>.
     * @param value The string value.
     */
    public ASTThis(Location location, String value)
    {
        super(location, value);
    }
}
