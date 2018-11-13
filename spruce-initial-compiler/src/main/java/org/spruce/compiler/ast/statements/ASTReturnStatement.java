package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTReturnStatement</code> is "return" optionally followed by an
 * expression, then a semicolon.</p>
 *
 * <em>
 * ReturnStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;return ;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;return Expression ;
 * </em>
 */
public class ASTReturnStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTReturnStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTReturnStatement(Location location, List<ASTNode> children)
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
