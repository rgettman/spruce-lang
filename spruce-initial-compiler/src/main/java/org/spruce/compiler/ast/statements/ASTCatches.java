package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCatches</code> is a list of catch clauses.</p>
 *
 * <em>
 * Catches:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CatchClause {CatchClause}
 * </em>
 */
public class ASTCatches extends ASTParentNode {
    /**
     * Constructs an <code>ASTCatches</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTCatches(Location location, List<ASTNode> children) {
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
