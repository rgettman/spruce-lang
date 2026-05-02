package org.spruce.compiler.bootstrap.ast.expressions;

/**
 * An <code>ASTValueExpression</code> is an expression that represents a value.
 * It can be a Binary Expression, a Cast Expression, a Unary Expression, an
 * Is-a Expression, or a Primary.
 * <em>
 * ValueExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BinaryExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CastExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;IsaExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UnaryExpression
 * </em>
 */
public sealed interface ASTValueExpression extends ASTExpression//, ASTElementValue
        permits ASTBinaryExpression, ASTCastExpression, /*ASTForExpression, ASTIfExpression,*/ ASTIsaExpression,
                /*ASTMapEntry,*/ ASTPrimary/*, ASTSwitchExpression*/, ASTUnaryExpression {
}
