package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCatchFormalParameter</code> is an optional variable
 * modifier list, a catch type, and an identifier.</p>
 *
 * <em>
 * LocalVariableDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList CatchType Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CatchType Identifier
 * </em>
 */
public class ASTCatchFormalParameter extends ASTParentNode
{
    /**
     * Constructs an <code>ASTCatchFormalParameter</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTCatchFormalParameter(Location location, List<ASTNode> children)
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
