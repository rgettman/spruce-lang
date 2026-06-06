package org.spruce.compiler.bootstrap.ast.statements;

import org.spruce.compiler.bootstrap.ast.ParentNode;

/**
 * An <code>ASTBlockStatement</code> is either a Statement, a Constructor
 * Invocation, or a LocalVariableDeclarationStatement.
 * <em>
 * BlockStatement:
 * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclarationStatement
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstructorInvocation
 * &nbsp;&nbsp;&nbsp;&nbsp;Statement
 * </em>
 */
public sealed interface ASTBlockStatement extends ParentNode permits ASTLocalVariableDeclarationStatement,
        ASTConstructorInvocation, ASTStatement {
}
