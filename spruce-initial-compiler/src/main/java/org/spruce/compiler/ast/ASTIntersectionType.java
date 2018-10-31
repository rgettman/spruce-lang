package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTIntersectionType</code> is an "&" separated list of DataTypes.</p>
 *
 * <em>
 * IntersectionType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType {& DataType}
 * </em>
 */
public class ASTIntersectionType extends ASTParentNode
{
    /**
     * Constructs an <code>ASTIntersectionType</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTIntersectionType(Location location, List<ASTNode> children)
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
