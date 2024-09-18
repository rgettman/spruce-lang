package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeArgumentList</code> is a comma-separated list of type
 * argument instances.</p>
 *
 * <em>
 * TypeArgumentList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeArgument {, TypeArgument}
 * </em>
 */
public class ASTTypeArgumentList extends ASTParentNode {
    /**
     * Constructs an <code>ASTTypeArgumentList</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTTypeArgumentList(Location location, List<ASTNode> children) {
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
