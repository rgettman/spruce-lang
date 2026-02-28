package org.spruce.compiler.bootstrap.ast.statements;

import org.spruce.compiler.bootstrap.ast.ParentNode;

/**
 * An <code>ASTStatement</code> is a block or one of many kinds of different
 * statements.
 * <em>
 * Statement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BreakStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ContinueStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ReturnStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;IfStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;WhileStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ForStatement
 * </em>
 */
public sealed interface ASTStatement extends ParentNode, ASTBlockStatement permits ASTBlock, ASTExpressionStatement,
        ASTBreakStatement, ASTContinueStatement, ASTReturnStatement,
        ASTIfStatement, ASTWhileStatement, ASTForStatement {
}
