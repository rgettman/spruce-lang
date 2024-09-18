package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTMutModifier</code> is "mut".</p>
 *
 * <em>
 * MutModifier:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;mut
 * </em>
 */
public class ASTMutModifier extends ASTParentNode {
    /**
     * Constructs an <code>ASTMutModifier</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTMutModifier(Location location, List<ASTNode> children) {
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
