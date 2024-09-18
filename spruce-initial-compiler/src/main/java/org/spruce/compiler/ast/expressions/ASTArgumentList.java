package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTArgumentList</code> is a comma-separated list of expressions.
 *
 * <em>
 * ArgumentList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Expression {, Expression}
 * </em>
 */
public class ASTArgumentList extends ASTParentNode {
    /**
     * Constructs an <code>ASTArgumentList</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTArgumentList(Location location, List<ASTNode> children) {
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
