package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTFinally</code> is "finally" followed by a Block.</p>
 *
 * <em>
 * Finally:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;finally Block
 * </em>
 */
public class ASTFinally extends ASTParentNode
{
    /**
     * Constructs an <code>ASTFinally</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTFinally(Location location, List<ASTNode> children)
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
