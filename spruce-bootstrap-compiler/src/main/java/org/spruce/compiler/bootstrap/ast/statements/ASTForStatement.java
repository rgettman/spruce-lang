package org.spruce.compiler.bootstrap.ast.statements;

import org.spruce.compiler.bootstrap.ast.ParentNode;

/**
 * An <code>ASTForStatement</code> is a particular kind of statement that is a
 * BasicForStatement or an EnhancedForStatement.
 * <em>
 * ForStatement:
 * &nbsp;&nbsp;&nbsp;&nbsp;BasicForStatement
 * &nbsp;&nbsp;&nbsp;&nbsp;EnhancedForStatement
 * </em>
 */
public sealed interface ASTForStatement extends ParentNode, ASTStatement permits ASTBasicForStatement, ASTEnhancedForStatement {
}
