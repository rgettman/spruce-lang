package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAssignment</code> is a left hand side, an assignment operator,
 * and an assignment expression.</p>
 *
 * <p>The operators associated with assignments are right-associative.</p>
 *
 * <em>
 * Assignment:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConditionalExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LeftHandSide AssignmentOperator AssignmentExpression
 * </em>
 * <em>
 * AssignmentOperator:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;(One of) = += -= *= /= %= &= |= ^= &lt;&lt;= &gt;&gt;=
 * </em>
 */
public class ASTAssignment extends ASTParentNode {
    /**
     * Constructs an <code>ASTAssignment</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTAssignment(Location location, List<ASTNode> children) {
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
