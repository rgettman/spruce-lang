package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTIntersectionType</code> is a data type or an intersection type,
 * "&", and another data type.</p>
 *
 * <p>The operators associated with intersection types are left-associative.</p>
 *
 * <em>
 * IntersectionType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;IntersectionType & DataType<br>
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
