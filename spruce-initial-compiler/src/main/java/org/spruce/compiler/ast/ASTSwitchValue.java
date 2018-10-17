package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchValue</code> is an expression (no incr/decr) or an
 * identifier.</p>
 *
 * <em>
 * SwitchValue:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionNoIncrDecr<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier
 * </em>
 */
public class ASTSwitchValue extends ASTParentNode
{
    /**
     * Constructs an <code>SwitchValue</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchValue(Location location, List<ASTNode> children)
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
