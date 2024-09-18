package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSingleElementAnnotation</code> is "@" followed by a TypeName,
 * then an element value within parentheses.</p>
 *
 * <em>
 * SingleElementAnnotation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;@ TypeName ( ElementValue )
 * </em>
 */
public class ASTSingleElementAnnotation extends ASTParentNode {
    /**
     * Constructs an <code>ASTSingleElementAnnotation</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSingleElementAnnotation(Location location, List<ASTNode> children) {
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
