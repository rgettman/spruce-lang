package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTBitwiseOrExpression</code> is a bitwise xor expression or
 * another bitwise or expression, '|', and a bitwise xor
 * expression.</p>
 *
 * <p>The operator associated with bitwise or expressions is left-associative.</p>
 *
 * <em>
 * BitwiseOrExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseXorExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseOrExpression | BitwiseXorExpression<br>
 * </em>
 */
public class ASTBitwiseOrExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTBitwiseOrExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTBitwiseOrExpression(Location location, List<ASTNode> children)
    {
        super(location, children);
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