package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAssignmentExpression</code> is a conditional expression or
 * an assignment.</p>
 *
 * <p>The operators associated with logical and expressions are right-associative.</p>
 *
 * <em>
 * AssignmentExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConditionalExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Assignment
 * </em>
 */
public class ASTAssignmentExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTAssignmentExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTAssignmentExpression(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return true;
    }
}
