package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTElementValueList</code> is a comma-separated list of
 * element values.</p>
 *
 * <em>
 * ElementValueList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ElementValue {, ElementValue}
 * </em>
 */
public class ASTElementValueList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTElementValueList</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTElementValueList(Location location, List<ASTNode> children)
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
