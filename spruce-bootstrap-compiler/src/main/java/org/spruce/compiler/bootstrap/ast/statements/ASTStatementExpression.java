package org.spruce.compiler.bootstrap.ast.statements;

import org.spruce.compiler.bootstrap.ast.ParentNode;
import org.spruce.compiler.bootstrap.ast.expressions.ASTClassInstanceCreationExpression;
import org.spruce.compiler.bootstrap.ast.expressions.ASTMethodInvocation;

/**
 * An <code>ASTStatementExpression</code> is an Assignment, a
 * MethodInvocationExpression, or a ClassInstanceCreationExpression.
 * <em>
 * StatementExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Assignment<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MethodInvocationExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression
 * </em>
 */
public sealed interface ASTStatementExpression extends ParentNode permits ASTAssignment,
        ASTMethodInvocation, ASTClassInstanceCreationExpression {
}
