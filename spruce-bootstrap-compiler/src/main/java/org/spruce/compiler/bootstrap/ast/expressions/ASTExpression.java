package org.spruce.compiler.bootstrap.ast.expressions;

import org.spruce.compiler.bootstrap.symbol.DataTypeResolution;
import org.spruce.compiler.bootstrap.ast.ParentNode;

/**
 * An <code>ASTExpression</code> is a general expression that could be a
 * LambdaExpression or a ValueExpression.
 * <em>
 * Expression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ValueExpression<br>
 * </em>
 */
public sealed interface ASTExpression extends ParentNode, ASTPrimaryChild, DataTypeResolution
        permits ASTValueExpression {
}
