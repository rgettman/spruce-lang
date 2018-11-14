package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTElementValuePairList</code> is a comma-separated list of
 * element value pairs.</p>
 *
 * <em>
 * ElementValuePairList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ElementValuePair {, ElementValuePair}
 * </em>
 */
public class ASTElementValuePairList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTElementValuePairList</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTElementValuePairList(Location location, List<ASTNode> children)
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