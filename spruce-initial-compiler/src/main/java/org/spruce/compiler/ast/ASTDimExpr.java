package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDimExpr</code> is an expression inside brackets.</p>
 *
 * <em>
 * DimExpr:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[ Expression ]
 * </em>
 */
public class ASTDimExpr extends ASTParentNode
{
    /**
     * Constructs an <code>ASTDims</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTDimExpr(Location location, List<ASTNode> children)
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
