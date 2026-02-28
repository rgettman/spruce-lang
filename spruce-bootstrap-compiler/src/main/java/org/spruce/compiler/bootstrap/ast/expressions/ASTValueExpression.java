package org.spruce.compiler.bootstrap.ast.expressions;

//import org.spruce.compiler.bootstrap.ast.classes.ASTElementValue;

/**
 * An <code>ASTValueExpression</code> is an expression that represents a value.
 * It can be an If Expression, a For Expression, a Binary Expression, a Unary
 * Expression, a Switch Expression, a MapEntry, or a Primary.
 * <em>
 * ValueExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;IfExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ForExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BinaryExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CastExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;IsaExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary
 * </em>
 */
public sealed interface ASTValueExpression extends ASTExpression//, ASTElementValue
        permits ASTBinaryExpression, /*ASTCastExpression, ASTForExpression, ASTIfExpression,*/ ASTIsaExpression,
                /*ASTMapEntry,*/ ASTPrimary/*, ASTSwitchExpression*/, ASTUnaryExpression {
}
