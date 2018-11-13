package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTMultiplicativeExpression</code> is a cast expression or
 * another multiplicative expression, a multiplicative operator, and a cast
 * expression.</p>
 *
 * <p>The operators associated with multiplicative expressions are left-associative.</p>
 *
 * <em>
 * MultiplicativeExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CastExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MultiplicativeExpression * CastExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MultiplicativeExpression / CastExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MultiplicativeExpression % CastExpression
 * </em>
 */
public class ASTMultiplicativeExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTMultiplicativeExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTMultiplicativeExpression(Location location, List<ASTNode> children)
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
