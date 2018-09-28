package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTBitwiseXorExpression</code> is a bitwise and expression or
 * another bitwise xor expression, '^', and a bitwise and
 * expression.</p>
 *
 * <p>The operator associated with bitwise xor expressions is left-associative.</p>
 *
 * <em>
 * BitwiseXorExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseAndExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BitwiseXorExpression ^ BitwiseAndExpression<br>
 * </em>
 */
public class ASTBitwiseXorExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTBitwiseXorExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTBitwiseXorExpression(Location location, List<ASTNode> children)
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
