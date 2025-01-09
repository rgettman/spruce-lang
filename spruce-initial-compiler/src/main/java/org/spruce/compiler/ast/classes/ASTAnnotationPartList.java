package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAnnotationPartList</code> is a list of annotation parts.</p>
 *
 * <em>
 * AnnotationPartList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationPart {AnnotationPart}
 * </em>
 */
public class ASTAnnotationPartList extends ASTListNode<ASTAnnotationPart> {
    /**
     * Constructs an <code>ASTAnnotationPartList</code> with a <code>Location</code>,
     * a list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTAnnotationPart</code>s.
     */
    public ASTAnnotationPartList(Location location, List<ASTAnnotationPart> children) {
        super(location, children, Type.ANNOTATION_PARTS);
    }
}
