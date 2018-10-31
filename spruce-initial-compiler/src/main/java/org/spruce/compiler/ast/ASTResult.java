package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTResult</code> is "void", or an optional ConstModifier
 * followed by a DataType.</p>
 *
 * <em>
 * Result:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstModifier DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;void<br>
 * </em>
 */
public class ASTResult extends ASTParentNode
{
    /**
     * Constructs an <code>ASTResult</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTResult(Location location, List<ASTNode> children)
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
