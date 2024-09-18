package org.spruce.compiler.ast.names;

import org.spruce.compiler.ast.ASTValueNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTIdentifier</code> is a simple name that may be combined with
 * other identifiers to form more complex names such as fully qualified names.</p>
 *
 * <em>
 * Identifier:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;IdentifierStart [IdentifierPart]*<br>
 * <br>
 * IdentifierStart:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;JavaLetter<br>
 * <br>
 * IdentifierPart:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;JavaLetterOrDigit
 * </em>
 */
public class ASTIdentifier extends ASTValueNode {
    /**
     * Constructs an <code>ASTIdentifier</code> given the <code>Location</code>
     * and the string value of the token.
     * @param location The <code>Location</code>.
     * @param value The string value.
     */
    public ASTIdentifier(Location location, String value) {
        super(location, value);
    }
}
