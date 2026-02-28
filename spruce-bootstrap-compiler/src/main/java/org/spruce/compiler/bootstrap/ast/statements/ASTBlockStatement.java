package org.spruce.compiler.bootstrap.ast.statements;

import org.spruce.compiler.bootstrap.ast.ParentNode;

/**
 * An <code>ASTBlockStatement</code> is either a Statement or a
 * LocalVariableDeclarationStatement.
 * <em>
 * BlockStatement:
 * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclarationStatement
 * &nbsp;&nbsp;&nbsp;&nbsp;Statement
 * </em>
 */
public sealed interface ASTBlockStatement extends ParentNode permits ASTLocalVariableDeclarationStatement, ASTStatement {
}
