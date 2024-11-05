package org.spruce.compiler.ast.statements;

import org.spruce.compiler.ast.ParentNode;

/**
 * An <code>ASTStatement</code> is a particular kind of statement that is a
 * BasicForStatement or an EnhancedForStatement.
 * <em>
 * Statement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BreakStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ContinueStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FallthroughStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AssertStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ReturnStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ThrowStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;IfStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;WhileStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DoWhileStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CriticalStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ForStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TryStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;YieldStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseStatement
 * </em>
 */
public sealed interface ASTStatement extends ParentNode, ASTBlockStatement permits ASTBlock, ASTExpressionStatement, ASTBreakStatement,
        ASTContinueStatement, ASTFallthroughStatement, ASTAssertStatement, ASTReturnStatement, ASTThrowStatement,
        ASTIfStatement, ASTWhileStatement, ASTDoStatement, ASTCriticalStatement, ASTForStatement, ASTTryStatement,
        ASTSwitchStatement, ASTYieldStatement, ASTUseStatement {
}
