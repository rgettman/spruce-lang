package org.spruce.compiler.ast.literals;

import org.spruce.compiler.ast.ASTValueNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTIntegerLiteral</code> is an integer value.</p>
 *
 * <em>
 * IntegerLiteral:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Digits<br>
 * <br>
 * Digits:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;0-9
 * </em>
 */
public class ASTIntegerLiteral extends ASTLiteral
{
    /**
     * Constructs an <code>ASTIntegerLiteral</code> given the <code>Location</code>
     * and the string value of the token.
     * @param location The <code>Location</code>.
     * @param value The string value.
     */
    public ASTIntegerLiteral(Location location, String value) {
        super(location, value);
    }

    /**
     * Returns the integer value.
     * @return The integer value.
     */
    public long getNumericValue() {
        return Long.parseLong(getValue());
    }
}
