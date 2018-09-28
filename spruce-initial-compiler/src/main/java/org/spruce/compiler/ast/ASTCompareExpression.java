package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTCompareExpression</code> is a bitwise or expression or
 * a bitwise or expression, '&lt;=&gt;', and another bitwise or
 * expression.</p>
 *
 * <p>The operator associated with compare expressions is NOT associative.</p>
 *
 * <em>
 * CompareExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseOrExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseOrExpression &lt;=&gt; BitwiseOrExpression<br>
 * </em>
 */
public class ASTCompareExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTCompareExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTCompareExpression(Location location, List<ASTNode> children)
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