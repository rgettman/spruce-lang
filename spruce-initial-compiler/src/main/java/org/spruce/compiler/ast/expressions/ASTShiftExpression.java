package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTShiftExpression</code> is an additive expression or
 * another shift expression, a shift operator, and an additive
 * expression.</p>
 *
 * <p>The operators associated with shift expressions are left-associative.</p>
 *
 * <em>
 * ShiftExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AdditiveExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ShiftExpression &lt;&lt; AdditiveExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ShiftExpression &gt;&gt; AdditiveExpression<br>
 * </em>
 */
public class ASTShiftExpression extends ASTParentNode {
    /**
     * Constructs an <code>ASTShiftExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTShiftExpression(Location location, List<ASTNode> children) {
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
