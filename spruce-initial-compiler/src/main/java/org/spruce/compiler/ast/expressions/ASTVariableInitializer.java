package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariableInitializer</code> is an expression (no incr/decr)
 * or an array initializer.</p>
 *
 * <em>
 * VariableInitializer:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionNoIncrDecr<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ArrayInitializer
 * </em>
 */
public class ASTVariableInitializer extends ASTParentNode {
    /**
     * Constructs an <code>ASTVariableInitializer</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTVariableInitializer(Location location, List<ASTNode> children) {
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
