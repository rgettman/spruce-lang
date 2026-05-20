package org.spruce.compiler.bootstrap.ast.literals;

import org.spruce.compiler.bootstrap.ast.ASTValueNode;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * <p>An <code>ASTCharacterLiteral</code> is exactly one character.</p>
 *
 * <em>
 * CharacterLiteral:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;' inputCharacter '<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;' escapeCharacter '<br>
 * </em>
 */
public final class ASTCharacterLiteral extends ASTValueNode implements ASTLiteral {
    private TypeSymbol myResolvedDataType;

    /**
     * Constructs an <code>ASTCharacterLiteral</code> given the <code>Location</code>
     * and the string value of the token.
     * @param location The <code>Location</code>.
     * @param value The string value.
     */
    public ASTCharacterLiteral(Location location, String value) {
        super(location, value);
    }

    /**
     * Returns the char value.
     * @return The char value.
     */
    public char getCharacterValue() {
        return getValue().charAt(0);
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
