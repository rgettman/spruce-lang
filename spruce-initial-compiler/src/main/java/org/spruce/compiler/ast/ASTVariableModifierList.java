package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariableModifierList</code> is a set variable modifiers.</p>
 *
 * <em>
 * VariableModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList VariableModifier
 * </em>
 */
public class ASTVariableModifierList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTVariableModifierList</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTVariableModifierList(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is NOT collapsible.
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return false;
    }
}
