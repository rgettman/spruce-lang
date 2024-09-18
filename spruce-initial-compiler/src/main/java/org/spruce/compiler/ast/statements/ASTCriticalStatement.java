package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCriticalStatement</code> is a "critical statement".
 * It is "critical" followed by a conditional expression and a block.</p>
 *
 * <em>
 * CriticalStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;critical ConditionalExpression Block
 * </em>
 */
public class ASTCriticalStatement extends ASTParentNode {
    /**
     * Constructs an <code>ASTSynchronizedStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTCriticalStatement(Location location, List<ASTNode> children) {
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
