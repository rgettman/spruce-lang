package org.spruce.compiler.ast.literals;

import java.util.Collections;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTValueNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLiteral</code> is a node representing a literal value.</p>
 *
 * <em>
 * Literal:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;IntegerLiteral<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FloatingPointLiteral<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CharacterLiteral<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;StringLiteral<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BooleanLiteral<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;NullLiteral
 * </em>
 */
public class ASTLiteral extends ASTValueNode {
    /**
     * Constructs an <code>ASTLiteral</code> at the given <code>Location</code>
     * and with one child as its node.
     * @param location The <code>Location</code>.
     * @param value The value of the node.
     */
    public ASTLiteral(Location location, String value) {
        super(location, value);
    }
}
