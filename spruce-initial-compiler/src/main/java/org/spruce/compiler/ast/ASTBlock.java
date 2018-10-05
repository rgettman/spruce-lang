package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTBlock</code> is a "{", optionally a list of block statements,
 * followed by a "}".</p>
 *
 * <em>
 * Block:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ BlockStatements }
 * </em>
 */
public class ASTBlock extends ASTParentNode
{
    /**
     * Constructs an <code>ASTBlockStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTBlock(Location location, List<ASTNode> children)
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
