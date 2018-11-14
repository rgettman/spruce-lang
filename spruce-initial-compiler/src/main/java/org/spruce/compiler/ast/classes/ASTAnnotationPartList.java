package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAnnotationPartList</code> is a list of annotation parts.</p>
 *
 * <em>
 * AnnotationPartList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationPart {AnnotationPart}
 * </em>
 */
public class ASTAnnotationPartList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTAnnotationPartList</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTAnnotationPartList(Location location, List<ASTNode> children)
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
