package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLambdaParameters</code> is either an identifier or a pair of
 * pipe characters with an optional lambda parameter list in between.</p>
 *
 * <em>
 * LambdaParameters:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;||<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;| [LambdaParameterList] |<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * </em>
 */
public class ASTLambdaParameters extends ASTParentNode {
    /**
     * Constructs an <code>ASTLambdaParameters</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTLambdaParameters(Location location, List<ASTNode> children) {
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
