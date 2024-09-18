package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeDeclaration</code> is a class, enum, interface, or
 * annotation declaration.</p>
 *
 * <em>
 * TypeDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EnumDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationDeclaration
 * </em>
 */
public class ASTTypeDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTTypeDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTTypeDeclaration(Location location, List<ASTNode> children) {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible() {
        return true;
    }
}

