package org.spruce.compiler.ast.expressions;

import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.names.ASTExpressionName;

/**
 * <p>An <code>ASTLeftHandSide</code> is an expression suitable for the left-
 * hand side of an assignment expression.</p>
 *
 * <em>
 * LeftHandSide:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ElementAccess<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FieldAccess
 * </em>
 */
public sealed interface ASTLeftHandSide extends ParentNode permits ASTExpressionName, ASTElementAccess, ASTFieldAccess {
}
