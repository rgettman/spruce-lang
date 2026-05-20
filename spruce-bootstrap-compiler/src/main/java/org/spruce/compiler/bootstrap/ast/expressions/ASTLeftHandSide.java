package org.spruce.compiler.bootstrap.ast.expressions;

import org.spruce.compiler.bootstrap.symbol.DataTypeResolution;
import org.spruce.compiler.bootstrap.ast.ParentNode;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;

/**
 * <p>An <code>ASTLeftHandSide</code> is a primary that is suitable for the
 * left-hand side of an assignment statement.</p>
 *
 * <em>
 * LeftHandSide:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FieldAccess
 * </em>
 */
public sealed interface ASTLeftHandSide extends ParentNode, DataTypeResolution permits ASTExpressionName, ASTFieldAccess {
}
