package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLogicalXorExpression</code> is a logical and expression or
 * a logical xor expression, '^:', and a logical and
 * expression.</p>
 *
 * <p>The operators associated with logical xor expressions are left-associative.</p>
 *
 * <em>
 * LogicalXorExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LogicalAndExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LogicalXorExpression ^: LogicalAndExpression
 * </em>
 */
public class ASTLogicalXorExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTLogicalXorExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTLogicalXorExpression(Location location, List<ASTNode> children)
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