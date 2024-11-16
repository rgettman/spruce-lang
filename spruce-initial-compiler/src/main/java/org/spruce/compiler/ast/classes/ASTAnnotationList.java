package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAnnotationList</code> is a list of annotations.</p>
 *
 * <em>
 * AnnotationList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Annotation {, Annotation}
 * </em>
 */
public class ASTAnnotationList extends ASTListNode<ASTAnnotation> {
    /**
     * Constructs an <code>Annotation</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTAnnotation</code>s.
     */
    public ASTAnnotationList(Location location, List<ASTAnnotation> children) {
        super(location, children, Type.ANNOTATIONS);
    }
}
