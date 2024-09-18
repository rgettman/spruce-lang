package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLambdaExpression</code> is lambda parameters, an arrow,
 * then a lambda body.</p>
 *
 * <em>
 * LambdaExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LambdaParameters -> LambdaBody<br>
 * </em>
 */
public class ASTLambdaExpression extends ASTParentNode {
    /**
     * Constructs an <code>ASTLambdaExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTLambdaExpression(Location location, List<ASTNode> children) {
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
