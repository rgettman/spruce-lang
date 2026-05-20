package org.spruce.compiler.bootstrap.ast.literals;

import org.spruce.compiler.bootstrap.ast.ASTValueNode;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

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
public final class ASTFloatingPointLiteral extends ASTValueNode implements ASTLiteral {
    private TypeSymbol myResolvedDataType;

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

    @Override
    public void setResolvedDataType(TypeSymbol symbol) {
        myResolvedDataType = symbol;
    }

    @Override
    public TypeSymbol getResolvedDataType() {
        return myResolvedDataType;
    }
}
