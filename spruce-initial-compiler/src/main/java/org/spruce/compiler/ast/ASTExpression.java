package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTExpression</code> is an expression, no incr / decr, or
 * a prefix / postfix increment / decrement expression.</p>
 *
 * <p>This element will only be found in the few places that will allow
 * increment and decrement expressions:</p>
 *
 * <ul>
 *     <li><code>return</code> statements</li>
 *     <li>the index for <code>ElementAccess</code> expressions</li>
 *     <li>arguments to <code>MethodInvocations</code></li>
 *     <li>dimensions to array creation expressions</li>
 * </ul>
 *
 * <em>
 * Expression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionNoIncrDecr<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;PrefixExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;PostfixExpression
 * </em>
 */
public class ASTExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTExpressionNoIncrDecr</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTExpression(Location location, List<ASTNode> children)
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
