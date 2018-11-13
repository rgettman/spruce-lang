package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCastExpression</code> is a unary expression or
 * another cast expression, "as", and an intersection type, which may be as
 * simple as a data type.</p>
 *
 * <p>The operators associated with cast expressions are left-associative.</p>
 *
 * <em>
 * CastExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UnaryExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CastExpression as IntersectionType<br>
 * </em>
 */
public class ASTCastExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTCastExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTCastExpression(Location location, List<ASTNode> children)
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
