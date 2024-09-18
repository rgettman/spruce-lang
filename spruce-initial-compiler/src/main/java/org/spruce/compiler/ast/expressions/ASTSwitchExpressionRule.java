package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchExpressionRule</code> is a switch label, then an arrow
 * (->), then either an expression, a block, or a throw statement.</p>
 *
 * <em>
 * SwitchExpressionRule:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> Expression ;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> ThrowStatement<br>
 * </em>
 */
public class ASTSwitchExpressionRule extends ASTParentNode {
    /**
     * Constructs an <code>ASTSwitchExpressionRule</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchExpressionRule(Location location, List<ASTNode> children) {
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