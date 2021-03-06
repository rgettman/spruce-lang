package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTShiftExpression</code> is an additive expression or
 * another shift expression, a shift operator, and an additive
 * expression.</p>
 *
 * <p>The operators associated with shift expressions are left-associative.</p>
 *
 * <em>
 * ShiftExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AdditiveExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ShiftExpression &lt;&lt; ShiftExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ShiftExpression &gt;&gt; ShiftExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ShiftExpression &gt;&gt;&gt; ShiftExpression<br>
 * </em>
 */
public class ASTShiftExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTShiftExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTShiftExpression(Location location, List<ASTNode> children)
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
