package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTResourceList</code> is a semicolon-separated list of resources.
 *
 * <em>
 * ResourceList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Resource {; Resource}
 * </em>
 */
public class ASTResourceList extends ASTParentNode {
    /**
     * Constructs an <code>ASTResourceList</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTResourceList(Location location, List<ASTNode> children) {
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
