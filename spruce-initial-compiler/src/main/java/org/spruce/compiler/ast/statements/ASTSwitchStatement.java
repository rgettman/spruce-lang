package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchStatement</code> is "switch", followed by an expression
 * in parentheses, followed by a Switch Block.</p>
 *
 * <em>
 * SwitchStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;switch ( Expression ) SwitchBlock
 * </em>
 */
public class ASTSwitchStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTSwitchStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchStatement(Location location, List<ASTNode> children)
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