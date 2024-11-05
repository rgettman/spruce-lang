package org.spruce.compiler.ast.expressions;

import org.spruce.compiler.ast.ParentNode;

/**
 * An <code>ASTVariableInitializer</code> is either an Expression or an
 * ArrayInitializer.
 * <em>
 * VariableInitializer:
 * &nbsp;&nbsp;&nbsp;&nbsp;Expression
 * &nbsp;&nbsp;&nbsp;&nbsp;ArrayInitializer
 * </em>
 */
public sealed interface ASTVariableInitializer extends ParentNode permits ASTExpression, ASTArrayInitializer {
}
