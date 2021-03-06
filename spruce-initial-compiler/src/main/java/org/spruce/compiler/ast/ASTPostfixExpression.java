package org.spruce.compiler.ast;

import java.util.Arrays;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTPostfixExpression</code> is a left hand side with a unary
 * increment or decrement operator.</p>
 *
 * <em>
 * PostfixExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LeftHandSide ++<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LeftHandSide --
 * </em>
 */
public class ASTPostfixExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTPostfixExpression</code> at the given <code>Location</code>
     * and with an increment or decrement operator, represented by the given
     * <code>TokenType</code>, and an <code>ASTLeftHandSide</code> as its child.
     * @param location The <code>Location</code>.
     * @param operand The operand, a <code>ASTLeftHandSide</code>.
     * @param operator The token type of the operator for this unary expression.
     */
    public ASTPostfixExpression(Location location, ASTLeftHandSide operand, TokenType operator)
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
