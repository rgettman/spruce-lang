package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchStatementBlock</code> is a "{", optionally switch
 * statement rules, followed by a "}".</p>
 *
 * <em>
 * SwitchStatementBlock:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ SwitchStatementRules }
 * </em>
 */
public class ASTSwitchStatementBlock extends ASTParentNode {
    /**
     * Constructs an <code>ASTSwitchStatementBlock</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchStatementBlock(Location location, List<ASTNode> children) {
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
