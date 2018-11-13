package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTFormalParameterList</code> is a comma-separated list of
 * formal parameter instances.  Only the last formal parameter may have an
 * ellipsis.</p>
 *
 * <em>
 * FormalParameterList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FormalParameter {, FormalParameter}
 * </em>
 */
public class ASTFormalParameterList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTFormalParameterList</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTFormalParameterList(Location location, List<ASTNode> children)
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
