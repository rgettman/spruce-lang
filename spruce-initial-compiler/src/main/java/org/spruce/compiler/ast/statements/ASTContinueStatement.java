package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTContinueStatement</code> is "continue" followed by a semicolon.</p>
 *
 * <em>
 * ContinueStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;continue ;<br>
 * </em>
 */
public class ASTContinueStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTContinueStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTContinueStatement(Location location, List<ASTNode> children)
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
