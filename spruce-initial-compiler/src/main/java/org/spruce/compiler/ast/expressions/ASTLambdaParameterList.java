package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLambdaParameterList</code> is either an inferred parameter
 * list or a formal parameter list.</p>
 *
 * <em>
 * LambdaParameterList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InferredParameterList<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FormalParameterList<br>
 * </em>
 */
public class ASTLambdaParameterList extends ASTParentNode {
    /**
     * Constructs an <code>ASTLambdaParameterList</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTLambdaParameterList(Location location, List<ASTNode> children) {
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
