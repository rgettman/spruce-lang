package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeParameterList</code> is a comma-separated list of type
 * parameter instances.</p>
 *
 * <em>
 * TypeParameterList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeParameter {, TypeParameter}
 * </em>
 */
public class ASTTypeParameterList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTTypeParameterList</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTTypeParameterList(Location location, List<ASTNode> children)
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
