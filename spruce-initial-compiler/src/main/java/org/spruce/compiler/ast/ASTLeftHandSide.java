package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLeftHandSide</code> is an expression suitable for the left-
 * hand side of an assignment expression.</p>
 *
 * <em>
 * LeftHandSide:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FieldAccess<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ElementAccess
 * </em>
 */
public class ASTLeftHandSide extends ASTParentNode
{
    /**
     * Constructs an <code>ASTLeftHandSide</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTLeftHandSide(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is not collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return true;
    }
}
