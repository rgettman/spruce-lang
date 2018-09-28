package org.spruce.compiler.ast;

import java.util.Arrays;

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
public class ASTLiteral extends ASTParentNode
{
    /**
     * Constructs an <code>ASTLiteral</code> at the given <code>Location</code>
     * and with one child as its node.
     * @param location The <code>Location</code>.
     * @param child The child node.
     */
    public ASTLiteral(Location location, ASTNode child)
    {
        super(location, Arrays.asList(child));
    }


    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return true;
    }
}
