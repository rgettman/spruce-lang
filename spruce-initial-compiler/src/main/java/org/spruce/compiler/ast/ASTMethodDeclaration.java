package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTMethodDeclaration</code> is an optional AccessModifier followed by
 * an optional MethodModifierList, then a MethodHeader and a MethodBody.</p>
 *
 * <em>
 * MethodDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] [MethodModifierList] MethodHeader MethodBody
 * </em>
 */
public class ASTMethodDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTMethodDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTMethodDeclaration(Location location, List<ASTNode> children)
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
