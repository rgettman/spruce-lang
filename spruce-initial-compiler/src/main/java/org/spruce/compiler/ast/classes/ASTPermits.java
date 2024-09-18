package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTPermits</code> is "permits" followed by a Data Type No Array List.</p>
 *
 * <em>
 * Permits:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;permits DataTypeNoArrayList
 * </em>
 */
public class ASTPermits extends ASTParentNode {
    /**
     * Constructs an <code>ASTPermits</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTPermits(Location location, List<ASTNode> children) {
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
