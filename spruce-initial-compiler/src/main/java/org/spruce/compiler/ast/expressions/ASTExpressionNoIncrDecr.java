package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTExpressionNoIncrDecr</code> is an assignment expression or
 * a lambda expression.  It excludes prefix and postfix increment and decrement
 * expressions.</p>
 *
 * <em>
 * ExpressionNoIncrDecr:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AssignmentExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LambdaExpression
 * </em>
 */
public class ASTExpressionNoIncrDecr extends ASTParentNode
{
    /**
     * Constructs an <code>ASTExpressionNoIncrDecr</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTExpressionNoIncrDecr(Location location, List<ASTNode> children)
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

    /**
     * Looks for something that can be the child of an <code>ASTLeftHandSide</code>.
     * If found, creates and returns the <code>ASTLeftHandSide</code>.
     * @return The <code>ASTLeftHandSide</code>.
     * @throws CompileException If no descendant node can be a child of an
     *     <code>ASTLeftHandSide</code>.
     */
    public ASTLeftHandSide getLeftHandSide()
    {
        return convertDescendant(Arrays.asList(ASTExpressionName.class, ASTElementAccess.class),
                ASTLeftHandSide::new,
                "Expected variable or element access.");
    }
}
