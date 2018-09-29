package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLocalVariableDeclaration</code> is an optional variable
 * modifier list, a local variable type, and a variable declarator list.</p>
 *
 * <em>
 * LocalVariableDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList LocalVariableType VariableDeclaratorList
 * </em>
 */
public class ASTLocalVariableDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTLocalVariableDeclaration</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTLocalVariableDeclaration(Location location, List<ASTNode> children)
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
