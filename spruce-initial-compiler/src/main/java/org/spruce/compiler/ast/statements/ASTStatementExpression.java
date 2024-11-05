package org.spruce.compiler.ast.statements;

import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.expressions.ASTClassInstanceCreationExpression;
import org.spruce.compiler.ast.expressions.ASTMethodInvocation;

/**
 * An <code>ASTStatementExpression</code> is an Assignment, a Postfix, a
 * MethodInvocationExpression, or a ClassInstanceCreationExpression.
 * <em>
 * StatementExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Assignment<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Postfix<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MethodInvocationExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression
 * </em>
 */
public sealed interface ASTStatementExpression extends ParentNode permits ASTAssignment, ASTPostfix,
        ASTMethodInvocation, ASTClassInstanceCreationExpression {
}
