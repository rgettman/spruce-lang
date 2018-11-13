package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTClassPart</code> is a shared constructor, a constructor, a
 * field declaration, a method declaration, a class declaration, an enum
 * declaration, an annotation declaration, or an interface declaration.</p>
 *
 * <em>
 * ClassPart:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SharedConstructor<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstructorDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FieldDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MethodDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EnumDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationDeclaration
 * </em>
 */
public class ASTClassPart extends ASTParentNode
{
    /**
     * Constructs an <code>ASTClassPart</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTClassPart(Location location, List<ASTNode> children)
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
