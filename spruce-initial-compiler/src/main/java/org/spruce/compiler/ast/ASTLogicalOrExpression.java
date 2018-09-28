package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTLogicalOrExpression</code> is a logical xor expression or
 * a logical or expression, a logical or operator, and a logical xor
 * expression.</p>
 *
 * <p>The operators associated with logical or expressions are left-associative.</p>
 *
 * <em>
 * LogicalOrExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LogicalXorExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LogicalOrExpression || LogicalXorExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LogicalOrExpression |: LogicalXorExpression
 * </em>
 */
public class ASTLogicalOrExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTLogicalOrExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTLogicalOrExpression(Location location, List<ASTNode> children)
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