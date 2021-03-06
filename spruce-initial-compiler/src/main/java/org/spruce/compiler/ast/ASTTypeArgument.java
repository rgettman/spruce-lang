package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTTypeArgument</code> is a wildcard or a reference type.</p>
 *
 * <em>
 * Wildcard:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Wildcard<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType
 * </em>
 */
public class ASTTypeArgument extends ASTParentNode
{
    /**
     * Constructs an <code>ASTWildcard</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTTypeArgument(Location location, List<ASTNode> children)
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
