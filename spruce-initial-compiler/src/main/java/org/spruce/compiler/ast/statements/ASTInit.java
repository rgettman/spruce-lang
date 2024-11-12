package org.spruce.compiler.ast.statements;

import org.spruce.compiler.ast.ParentNode;

/**
 * <p>An <code>ASTInit</code> is a local variable declaration
 * or a statement expression list.</p>
 *
 * <em>
 * Init:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpressionList
 * </em>
 */
public sealed interface ASTInit extends ParentNode permits ASTLocalVariableDeclaration, ASTStatementExpressionList {
}
