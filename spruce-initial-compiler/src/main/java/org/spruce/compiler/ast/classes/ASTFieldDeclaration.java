package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTFieldDeclaration</code> is an optional AccessModifier followed by
 * an optional FieldModifierList, a DataType, and a VariableDeclaratorList.</p>
 *
 * <em>
 * FieldDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] [FieldModifierList] DataType VariableDeclaratorList
 * </em>
 */
public class ASTFieldDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTFieldDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTFieldDeclaration(Location location, List<ASTNode> children)
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
