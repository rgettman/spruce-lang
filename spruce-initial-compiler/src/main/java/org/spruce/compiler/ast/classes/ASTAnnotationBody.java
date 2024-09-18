package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAnnotationBody</code> is a "{", optionally a list of annotation parts,
 * followed by a "}".</p>
 *
 * <em>
 * AnnotationBody:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ AnnotationPartList }
 * </em>
 */
public class ASTAnnotationBody extends ASTParentNode {
    /**
     * Constructs an <code>ASTAnnotationBody</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTAnnotationBody(Location location, List<ASTNode> children) {
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
