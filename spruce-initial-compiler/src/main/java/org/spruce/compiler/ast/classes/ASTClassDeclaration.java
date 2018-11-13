package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTClassDeclaration</code> is an optional AccessModifier followed by
 * an optional ClassModifierList, then "class", an Identifier, followed by
 * optional Type Parameters, optional Superclass, optional Superinterfaces,
 * then a ClassBody.</p>
 *
 * <em>
 * ClassDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] [ClassModifierList] class Identifier [TypeParameters] [Superclass] [Superinterfaces] ClassBody
 * </em>
 */
public class ASTClassDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTClassDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTClassDeclaration(Location location, List<ASTNode> children)
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
