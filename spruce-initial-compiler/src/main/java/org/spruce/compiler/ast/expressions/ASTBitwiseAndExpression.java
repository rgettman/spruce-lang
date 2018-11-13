package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTBitwiseAndExpression</code> is a shift expression or
 * another bitwise and expression, '&amp;', and a shift
 * expression.</p>
 *
 * <p>The operator associated with bitwise and expressions is left-associative.</p>
 *
 * <em>
 * BitwiseAndExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ShiftExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseAndExpression &amp; ShiftExpression<br>
 * </em>
 */
public class ASTBitwiseAndExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTBitwiseAndExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTBitwiseAndExpression(Location location, List<ASTNode> children)
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
