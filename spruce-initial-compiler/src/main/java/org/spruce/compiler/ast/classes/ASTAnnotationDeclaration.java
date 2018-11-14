package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAnnotationDeclaration</code> is an optional AccessModifier followed by
 * an optional InterfaceModifierList, then "annotation", an Identifier, then an AnnotationBody.</p>
 *
 * <em>
 * AnnotationDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] [InterfaceModifierList] annotation Identifier AnnotationBody
 * </em>
 */
public class ASTAnnotationDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTAnnotationDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTAnnotationDeclaration(Location location, List<ASTNode> children)
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
