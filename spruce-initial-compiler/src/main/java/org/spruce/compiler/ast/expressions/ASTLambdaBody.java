package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLambdaBody</code> is an expression or a block.</p>
 *
 * <em>
 * LambdaBody:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Expression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Block<br>
 * </em>
 */
public class ASTLambdaBody extends ASTParentNode {
    /**
     * Constructs an <code>ASTLambdaBody</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTLambdaBody(Location location, List<ASTNode> children) {
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