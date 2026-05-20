package org.spruce.compiler.bootstrap.ast.literals;

import org.spruce.compiler.bootstrap.ast.ASTValueNode;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * <p>An <code>ASTStringLiteral</code> is a string of characters.</p>
 *
 * <em>
 * StringLiteral:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;" StringCharacter "<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;""" InputCharacter """<br>
 * <br>
 * StringCharacter:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InputCharacter ( but not " or \ )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EscapeCharacter
 * </em>
 */
public final class ASTStringLiteral extends ASTValueNode implements ASTLiteral {
    private TypeSymbol myResolvedDataType;

    /**
     * Constructs an <code>ASTStringLiteral</code> given the <code>Location</code>
     * and the string value of the token.
     * @param location The <code>Location</code>.
     * @param value The string value.
     */
    public ASTStringLiteral(Location location, String value) {
        super(location, value);
    }

    /**
     * Returns the string value.
     * @return The string value.
     */
    public String getStringValue() {
        return getValue();
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
