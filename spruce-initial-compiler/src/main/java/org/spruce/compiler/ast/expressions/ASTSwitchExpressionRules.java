package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTSwitchExpressionRules</code> is a list of switch expression rules.</p>
 *
 * <em>
 * SwitchExpressionRules:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchExpressionRule {SwitchExpressionRule}<br>
 * </em>
 */
public class ASTSwitchExpressionRules extends ASTListNode<ASTSwitchExpressionRule> {
    /**
     * Constructs an <code>ASTSwitchExpressionRules</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTSwitchExpressionRule</code>s.
     */
    public ASTSwitchExpressionRules(Location location, List<ASTSwitchExpressionRule> children) {
        super(location, children, Type.SWITCH_EXPR_RULES);
    }
}
