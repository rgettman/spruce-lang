package org.spruce.compiler.bootstrap.ast.expressions;

import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.symbol.DataTypeResolution;
import org.spruce.compiler.bootstrap.ast.literals.ASTLiteral;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;

/**
 * An <code>ASTPrimaryChild</code> is a <code>Node</code> that is suitable to
 * be a child of an <code>ASTPrimary</code>.
 * @see ASTPrimary
 */
public sealed interface ASTPrimaryChild extends Node, DataTypeResolution permits
        ASTPrimary.ASTBadPrimary, ASTClassInstanceCreationExpression, ASTClassLiteral, ASTExpression,
        ASTExpressionName, ASTFieldAccess, ASTLiteral, ASTMethodInvocation,
        ASTSelf, ASTTypenameSelf {
}
