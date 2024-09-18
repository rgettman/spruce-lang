package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchExpressionRules</code> is a list of switch expression rules.</p>
 *
 * <em>
 * SwitchExpressionRules:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchExpressionRule {SwitchExpressionRule}<br>
 * </em>
 */
public class ASTSwitchExpressionRules extends ASTParentNode {
    /**
     * Constructs an <code>ASTSwitchExpressionRules</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchExpressionRules(Location location, List<ASTNode> children) {
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
