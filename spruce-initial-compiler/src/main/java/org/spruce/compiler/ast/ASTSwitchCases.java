package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchCases</code> is a list of Switch Case instances.</p>
 *
 * <em>
 * SwitchCases:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchCase {SwitchCase}
 * </em>
 */
public class ASTSwitchCases extends ASTParentNode
{
    /**
     * Constructs an <code>ASTSwitchCases</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchCases(Location location, List<ASTNode> children)
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
