package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchValues</code> is comma-separated list of switch values.</p>
 *
 * <em>
 * SwitchValues:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchValue {, SwitchValue}
 * </em>
 */
public class ASTSwitchValues extends ASTParentNode
{
    /**
     * Constructs an <code>SwitchValues</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchValues(Location location, List<ASTNode> children)
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
