package org.spruce.compiler.ast.literals;

import org.spruce.compiler.ast.ASTValueNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCharacterLiteral</code> is exactly one character.</p>
 *
 * <em>
 * CharacterLiteral:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;' inputCharacter '<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;' escapeCharacter '<br>
 * </em>
 */
public class ASTCharacterLiteral extends ASTLiteral {
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
}
