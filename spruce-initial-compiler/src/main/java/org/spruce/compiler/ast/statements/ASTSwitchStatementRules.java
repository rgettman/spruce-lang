package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchStatementRules</code> is a list of switch statement rules.</p>
 *
 * <em>
 * SwitchStatementRules:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchStatementRule {SwitchStatementRule}<br>
 * </em>
 */
public class ASTSwitchStatementRules extends ASTParentNode {
    /**
     * Constructs an <code>ASTSwitchStatementRules</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchStatementRules(Location location, List<ASTNode> children) {
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
