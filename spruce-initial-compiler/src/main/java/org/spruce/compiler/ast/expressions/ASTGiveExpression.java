package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTGiveExpression</code> is an expression optionally preceded by
 * "give".</p>
 *
 * <em>
 * Expression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Expression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;give Expression<br>
 * </em>
 */
public class ASTGiveExpression extends ASTParentNode {
    /**
     * Constructs an <code>ASTGiveExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTGiveExpression(Location location, List<ASTNode> children) {
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
