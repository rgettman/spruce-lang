package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTClassPartList</code> is a list of class parts.</p>
 *
 * <em>
 * ClassPartList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassPart {ClassPart}
 * </em>
 */
public class ASTClassPartList extends ASTParentNode {
    /**
     * Constructs an <code>ASTClassPartList</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTClassPartList(Location location, List<ASTNode> children) {
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
