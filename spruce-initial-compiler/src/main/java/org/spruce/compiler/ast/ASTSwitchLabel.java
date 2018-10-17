package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchLabel</code> is "case" followed by Switch Values and a
 * colon, or "default" followed by a colon.</p>
 *
 * <em>
 * SwitchValue:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;case SwitchValues :<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;default :
 * </em>
 */
public class ASTSwitchLabel extends ASTParentNode
{
    /**
     * Constructs an <code>ASTSwitchLabel</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchLabel(Location location, List<ASTNode> children)
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
