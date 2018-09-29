package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariableInitializerList</code> is one or more comma-separated
 * expressions (no incr/decr).</p>
 *
 * <p>The operators associated with variable initializer lists are left-associative.</p>
 *
 * <em>
 * VariableInitializerList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableInitializer<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableInitializerList , VariableInitializer
 * </em>
 */
public class ASTVariableInitializerList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTVariableInitializerList</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTVariableInitializerList(Location location, List<ASTNode> children)
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
