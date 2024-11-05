package org.spruce.compiler.ast.statements;

import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.expressions.ASTFieldAccess;
import org.spruce.compiler.ast.names.ASTExpressionName;

/**
 * An <code>ASTForStatement</code> is a particular kind of statement that is a
 * BasicForStatement or an EnhancedForStatement.
 * <em>
 * Resource:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ResourceDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FieldAccess
 * </em>
 */
public sealed interface ASTResource extends ParentNode permits ASTResourceDeclaration, ASTExpressionName, ASTFieldAccess {
}
