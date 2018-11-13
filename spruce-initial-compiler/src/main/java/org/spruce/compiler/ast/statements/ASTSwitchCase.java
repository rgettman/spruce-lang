package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchCase</code> is a Switch Label optionally followed by Block Statements.</p>
 *
 * <em>
 * SwitchCase:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel BlockStatements
 * </em>
 */
public class ASTSwitchCase extends ASTParentNode
{
    /**
     * Constructs an <code>ASTSwitchCase</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchCase(Location location, List<ASTNode> children)
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
