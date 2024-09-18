package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAnnotation</code> is a marker annotation, a single element
 * annotation, or a normal annotation.</p>
 *
 * <em>
 * Annotation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MarkerAnnotation<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SingleElementAnnotation<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;NormalAnnotation
 * </em>
 */
public class ASTAnnotation extends ASTParentNode {
    /**
     * Constructs an <code>ASTAnnotation</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTAnnotation(Location location, List<ASTNode> children) {
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
