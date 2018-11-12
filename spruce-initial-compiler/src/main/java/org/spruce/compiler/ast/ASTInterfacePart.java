package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTInterfacePart</code> is a constant declaration, an interface
 * method declaration, a class declaration, an enum declaration, an annotation
 * declaration, or an interface declaration.</p>
 *
 * <em>
 * InterfacePart:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstantDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceMethodDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EnumDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationDeclaration
 * </em>
 */
public class ASTInterfacePart extends ASTParentNode
{
    /**
     * Constructs an <code>ASTInterfacePart</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTInterfacePart(Location location, List<ASTNode> children)
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
