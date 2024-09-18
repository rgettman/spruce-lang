package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTNormalAnnotation</code> is "@" followed by a TypeName,
 * then an optional element value pair list within parentheses.</p>
 *
 * <em>
 * NormalAnnotation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;@ TypeName ( [ElementValuePairList] )
 * </em>
 */
public class ASTNormalAnnotation extends ASTParentNode {
    /**
     * Constructs an <code>ASTNormalAnnotation</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTNormalAnnotation(Location location, List<ASTNode> children) {
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
