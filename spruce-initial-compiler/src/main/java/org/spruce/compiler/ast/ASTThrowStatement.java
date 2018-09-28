package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTThrowStatement</code> is "throw" followed by an expression,
 * then a semicolon.</p>
 *
 * <em>
 * ThrowStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;throw Expression ;
 * </em>
 */
public class ASTThrowStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTThrowStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTThrowStatement(Location location, List<ASTNode> children)
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
