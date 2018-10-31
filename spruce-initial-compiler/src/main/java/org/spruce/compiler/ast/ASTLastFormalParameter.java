package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLastFormalParameter</code> is an optional variable modifier list,
 * a data type, an ellipsis, and an identifier.</p>
 *
 * <em>
 * FormalParameter:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList DataType ... Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType ... Identifier
 * </em>
 */
public class ASTLastFormalParameter extends ASTParentNode
{
    /**
     * Constructs an <code>ASTLastFormalParameter</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTLastFormalParameter(Location location, List<ASTNode> children)
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
