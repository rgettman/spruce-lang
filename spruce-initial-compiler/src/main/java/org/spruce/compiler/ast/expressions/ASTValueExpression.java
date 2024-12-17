package org.spruce.compiler.ast.expressions;

import org.spruce.compiler.ast.classes.ASTElementValue;

/**
 * An <code>ASTValueExpression</code> is an expression that represents a value.
 * It can be a Conditional Expression, a Binary Expression, a Unary Expression,
 * a Switch Expression, or a Primary.
 * <em>
 * ValueExpression:
 * &nbsp;&nbsp;&nbsp;&nbsp;ConditionalExpression
 * &nbsp;&nbsp;&nbsp;&nbsp;BinaryExpression
 * &nbsp;&nbsp;&nbsp;&nbsp;UnaryExpression
 * &nbsp;&nbsp;&nbsp;&nbsp;CastExpression
 * &nbsp;&nbsp;&nbsp;&nbsp;IsaExpression
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchExpression
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary
 * </em>
 */
public sealed interface ASTValueExpression extends ASTExpression, ASTElementValue
        permits ASTIfExpression, ASTBinaryExpression, ASTUnaryExpression,
        ASTCastExpression, ASTIsaExpression, ASTSwitchExpression, ASTPrimary {
}
