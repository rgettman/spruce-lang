package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAnnotationPart</code> is an annotation type
 * member declaration, a constant declaration, a class declaration, or an
 * interface declaration.</p>
 *
 * <em>
 * AnnotationPart:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationTypeElementDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstantDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EnumDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationDeclaration
 * </em>
 */
public class ASTAnnotationPart extends ASTParentNode {
    /**
     * Constructs an <code>ASTAnnotationPart</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTAnnotationPart(Location location, List<ASTNode> children) {
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
