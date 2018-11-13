package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTBreakStatement</code> is "break" followed by a semicolon.</p>
 *
 * <em>
 * BreakStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;break ;<br>
 * </em>
 */
public class ASTBreakStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTBreakStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTBreakStatement(Location location, List<ASTNode> children)
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
