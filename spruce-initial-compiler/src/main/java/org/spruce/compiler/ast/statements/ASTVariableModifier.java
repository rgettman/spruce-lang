package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariableModifier</code> is "var" or "mut".</p>
 *
 * <em>
 * VariableModifier:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;var<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;mut
 * </em>
 */
public class ASTVariableModifier extends ASTParentNode
{
    /**
     * Constructs an <code>ASTVariableModifier</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTVariableModifier(Location location, List<ASTNode> children)
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
