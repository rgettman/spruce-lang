package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTResourceDeclaration</code> is an optional variable modifier
 * list, a local variable type, an identifier, an assignment, and an
 * expression (no incr/decr).</p>
 *
 * <em>
 * ResourceDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList LocalVariableType Identifier := ExpressionNoIncrDecr<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableType Identifier := ExpressionNoIncrDecr
 * </em>
 */
public class ASTResourceDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTResourceDeclaration</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTResourceDeclaration(Location location, List<ASTNode> children)
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
