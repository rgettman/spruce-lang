package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTFormalParameters</code> is comma-separated list of formal parameter instances.</p>
 *
 * <em>
 * FormalParameters:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FormalParameter {, FormalParameter}
 * </em>
 */
public class ASTFormalParameters extends ASTParentNode
{
    /**
     * Constructs an <code>ASTFormalParameters</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTFormalParameters(Location location, List<ASTNode> children)
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
