package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCaseConstants</code> is a comma-separated list of
 * conditional expressions.</p>
 *
 * <em>
 * CaseConstants:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConditionalExpression {, ConditionalExpression}
 * </em>
 */
public class ASTCaseConstants extends ASTParentNode {
    /**
     * Constructs an <code>ASTCaseConstants</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTCaseConstants(Location location, List<ASTNode> children) {
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
