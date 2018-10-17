package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchBlock</code> is a "{", optionally a list of switch cases,
 * followed by a "}".</p>
 *
 * <em>
 * SwitchBlock:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ SwitchCases }
 * </em>
 */
public class ASTSwitchBlock extends ASTParentNode
{
    /**
     * Constructs an <code>ASTSwitchBlock</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchBlock(Location location, List<ASTNode> children)
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
