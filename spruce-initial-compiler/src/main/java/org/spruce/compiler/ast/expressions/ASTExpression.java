package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTExpression</code> is a conditional expression or
 * a lambda expression.</p>
 *
 * <em>
 * Expression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConditionalExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LambdaExpression<br>
 * </em>
 */
public class ASTExpression extends ASTParentNode {
    /**
     * Constructs an <code>ASTExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTExpression(Location location, List<ASTNode> children) {
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
