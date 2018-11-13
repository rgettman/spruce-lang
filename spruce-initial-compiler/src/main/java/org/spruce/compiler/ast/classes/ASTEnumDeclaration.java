package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTEnumDeclaration</code> is an optional AccessModifier followed by
 * an optional ClassModifierList, then "enum", an Identifier, followed by
 * optional Superinterfaces, then an EnumBody.</p>
 *
 * <em>
 * EnumDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] [ClassModifierList] enum Identifier [Superinterfaces] EnumBody
 * </em>
 */
public class ASTEnumDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTEnumDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTEnumDeclaration(Location location, List<ASTNode> children)
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
