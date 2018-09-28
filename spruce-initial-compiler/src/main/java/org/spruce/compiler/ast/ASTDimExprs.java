package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDimExprs</code> is a set of dim expr instances.</p>
 *
 * <em>
 * DimExprs:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DimExpr<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DimExprs DimExpr
 * </em>
 */
public class ASTDimExprs extends ASTParentNode
{
    /**
     * Constructs an <code>ASTDims</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTDimExprs(Location location, List<ASTNode> children)
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
