package org.spruce.compiler.ast.literals;

import org.spruce.compiler.ast.ASTValueNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTFloatingPointLiteral</code> is a floating point decimal number.</p>
 *
 * <em>
 * FloatingPointLiteral:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Digits . [Digits] [ExponentPart]<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[Digits] . Digits [ExponentPart]<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Digits ExponentPart<br>
 * <br>
 * Digits:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;0-9<br>
 * <br>
 * ExponentPart:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;e|E[+|-][Digits]\
 * </em>
 */
public class ASTFloatingPointLiteral extends ASTLiteral {
    /**
     * Constructs an <code>ASTFloatingPointLiteral</code> given the <code>Location</code>
     * and the string value of the token.
     * @param location The <code>Location</code>.
     * @param value The string value.
     */
    public ASTFloatingPointLiteral(Location location, String value) {
        super(location, value);
    }

    /**
     * Returns the floating point value.
     * @return The floating point value.
     */
    public double getNumericValue() {
        return Double.parseDouble(getValue());
    }
}
