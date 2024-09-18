package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAdditiveExpression</code> is a multiplicative expression or
 * another additive expression, an additive operator, and a multiplicative
 * expression.</p>
 *
 * <p>The operators associated with additive expressions are left-associative.</p>
 *
 * <em>
 * AdditiveExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MultiplicativeExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AdditiveExpression + MultiplicativeExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AdditiveExpression - MultiplicativeExpression<br>
 * </em>
 */
public class ASTAdditiveExpression extends ASTParentNode {
    /**
     * Constructs an <code>ASTAdditiveExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTAdditiveExpression(Location location, List<ASTNode> children) {
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
