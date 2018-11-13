package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariableModifierList</code> is a list of variable modifiers.</p>
 *
 * <em>
 * VariableModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifier {VariableModifier}
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
