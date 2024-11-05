package org.spruce.compiler.ast.expressions;

/**
 * An <code>ASTExpression</code> is a general expression that could be a
 * LambdaExpression, a ValueExpression, or a GiveExpression.
 */
public sealed interface ASTExpression extends ASTLambdaBody, ASTVariableInitializer permits ASTLambdaExpression, ASTValueExpression {
}
