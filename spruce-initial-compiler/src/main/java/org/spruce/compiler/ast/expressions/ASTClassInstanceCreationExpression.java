package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTClassInstanceCreationExpression</code> is an unqualified
 * class instance creation expression that may be preceded by a primary and ".".
 *
 * <em>
 * ClassInstanceCreationExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UnqualifiedClassInstanceCreationExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary . UnqualifiedClassInstanceCreationExpression
 * </em>
 */
public class ASTClassInstanceCreationExpression extends ASTParentNode {
    /**
     * Constructs an <code>ASTClassInstanceCreationExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTClassInstanceCreationExpression(Location location, List<ASTNode> children) {
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
