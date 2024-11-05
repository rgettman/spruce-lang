package org.spruce.compiler.ast.statements;

import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.expressions.ASTBinaryExpression;
import org.spruce.compiler.ast.expressions.ASTCastExpression;
import org.spruce.compiler.ast.expressions.ASTConditionalExpression;
import org.spruce.compiler.ast.expressions.ASTExpression;
import org.spruce.compiler.ast.expressions.ASTIsaExpression;
import org.spruce.compiler.ast.expressions.ASTPrimary;
import org.spruce.compiler.ast.expressions.ASTSwitchExpression;
import org.spruce.compiler.ast.expressions.ASTUnaryExpression;

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
