package org.spruce.compiler.ast.expressions;

/**
 * An <code>ASTExpression</code> is a general expression that could be a
 * LambdaExpression or a ValueExpression.
 */
public sealed interface ASTExpression extends ASTLambdaBody, ASTVariableInitializer permits ASTLambdaExpression, ASTValueExpression {
}
