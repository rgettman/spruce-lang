package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTElementValuePair</code> is an identifier, the assignment
 * operator, and an element value.</p>
 *
 * <em>
 * ElementValuePair:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier := ElementValue
 * </em>
 */
public class ASTElementValuePair extends ASTParentNode
{
    /**
     * Constructs an <code>ASTElementValuePair</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTElementValuePair(Location location, List<ASTNode> children)
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
