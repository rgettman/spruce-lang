package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSuperclass</code> is "extends" followed by a Data Type (no array).</p>
 *
 * <em>
 * Superclass:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;extends DataTypeNoArray
 * </em>
 */
public class ASTSuperclass extends ASTParentNode {
    /**
     * Constructs an <code>ASTSuperclass</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSuperclass(Location location, List<ASTNode> children) {
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
