package org.spruce.compiler.ast.expressions;

import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.statements.ASTBlock;

/**
 * An <code>ASTLambdaBody</code> is an Expression or a Block.
 * <em>
 * ASTLambdaBody:
 * &nbsp;&nbsp;&nbsp;&nbsp;Expression
 * &nbsp;&nbsp;&nbsp;&nbsp;Block
 * </em>
 */
public sealed interface ASTLambdaBody extends ParentNode permits ASTExpression, ASTBlock {
}
