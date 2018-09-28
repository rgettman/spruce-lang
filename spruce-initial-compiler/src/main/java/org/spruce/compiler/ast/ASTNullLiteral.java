package org.spruce.compiler.ast;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTNullLiteral</code> is the value <code>null</code>.</p>
 *
 * <em>
 * NullLiteral:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;null
 * </em>
 */
public class ASTNullLiteral extends ASTValueNode
{
    /**
     * Constructs an <code>ASTNullLiteral</code> given the <code>Location</code>
     * and the string value of the token.
     * @param location The <code>Location</code>.
     * @param value The string value.
     */
    public ASTNullLiteral(Location location, String value)
    {
        super(location, value);
    }

    /**
     * Returns <code>null</code>.
     * @return <code>null</code>.
     */
    public Object getNullValue()
    {
        return null;
    }
}
