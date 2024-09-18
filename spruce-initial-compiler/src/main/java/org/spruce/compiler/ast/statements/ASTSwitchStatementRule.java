package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchStatementRule</code> is a switch label, then an arrow
 * (->), then either an expression statement, a block, or a throw statement.</p>
 *
 * <em>
 * SwitchStatementRule:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> ExpressionStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> ThrowStatement<br>
 * </em>
 */
public class ASTSwitchStatementRule extends ASTParentNode {
    /**
     * Constructs an <code>ASTSwitchStatementRule</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchStatementRule(Location location, List<ASTNode> children) {
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