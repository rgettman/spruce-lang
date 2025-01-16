package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTSwitchStatementRules</code> is a list of switch statement rules.</p>
 *
 * <em>
 * SwitchStatementRules:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchStatementRule {SwitchStatementRule}<br>
 * </em>
 */
public class ASTSwitchStatementRules extends ASTListNode<ASTSwitchStatementRule> {
    /**
     * Constructs an <code>ASTSwitchStatementRules</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTSwitchStatementRule</code>s.
     */
    public ASTSwitchStatementRules(Location location, List<ASTSwitchStatementRule> children) {
        super(location, children, Type.SWITCH_STMT_RULES);
    }
}
