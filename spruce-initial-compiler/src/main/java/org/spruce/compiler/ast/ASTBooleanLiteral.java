package org.spruce.compiler.ast;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTBooleanLiteral</code> is <code>true</code> or <code>false</code>.</p>
 *
 * <em>
 * BooleanLiteral:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;true<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;false
 * </em>
 */
public class ASTBooleanLiteral extends ASTValueNode
{
    /**
     * Constructs an <code>ASTBooleanLiteral</code> given the <code>Location</code>
     * and the string value of the token.
     * @param location The <code>Location</code>.
     * @param value The string value.
     */
    public ASTBooleanLiteral(Location location, String value)
    {
        super(location, value);
    }

    /**
     * Returns the boolean value.
     * @return The boolean value.
     */
    public boolean getBooleanValue()
    {
        return Boolean.parseBoolean(getValue());
    }
}
