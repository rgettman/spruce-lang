package org.spruce.compiler.ast.expressions;

/**
 * An <code>ASTExpression</code> is a general expression that could be a
 * LambdaExpression or a ValueExpression.
 * <em>
 * Expression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ValueExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LambdaExpression
 * </em>
 */
public sealed interface ASTExpression extends ASTLambdaBody permits ASTLambdaExpression, ASTValueExpression {
}
