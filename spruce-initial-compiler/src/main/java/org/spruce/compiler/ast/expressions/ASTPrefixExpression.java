package org.spruce.compiler.ast.expressions;

import java.util.Arrays;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTPrefixExpression</code> is a unary increment or decrement
 * operator with a left hand side.</p>
 *
 * <em>
 * PrefixExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;++ LeftHandSide<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;-- LeftHandSide
 * </em>
 */
public class ASTPrefixExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTPrefixExpression</code> at the given <code>Location</code>
     * and with an increment or decrement operator, represented by the given
     * <code>TokenType</code>, and an <code>ASTLeftHandSide</code> as its child.
     * @param location The <code>Location</code>.
     * @param operand The operand, a <code>ASTLeftHandSide</code>.
     * @param operator The token type of the operator for this unary expression.
     */
    public ASTPrefixExpression(Location location, ASTLeftHandSide operand, TokenType operator)
    {
        super(location, Arrays.asList(operand), operator);
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
