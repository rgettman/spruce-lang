package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchExpressionBlock</code> is a "{", followed by a list of switch
 * expression rules, followed by a "}".</p>
 *
 * <em>
 * SwitchExpressionBlock:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ SwitchExpressionRules }
 * </em>
 */
public class ASTSwitchExpressionBlock extends ASTParentNode {
    /**
     * Constructs an <code>ASTSwitchExpressionBlock</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSwitchExpressionBlock(Location location, List<ASTNode> children) {
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
