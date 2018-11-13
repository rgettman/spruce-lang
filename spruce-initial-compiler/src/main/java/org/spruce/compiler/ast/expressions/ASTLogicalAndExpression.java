package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLogicalAndExpression</code> is a relational expression or
 * a logical and expression, logical and operator, and a relational
 * expression.</p>
 *
 * <p>The operators associated with logical and expressions are left-associative.</p>
 *
 * <em>
 * LogicalAndExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LogicalAndExpression &amp;&amp; RelationalExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LogicalAndExpression &amp;: RelationalExpression
 * </em>
 */
public class ASTLogicalAndExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTLogicalAndExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTLogicalAndExpression(Location location, List<ASTNode> children)
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