package org.spruce.compiler.ast.expressions;

import org.spruce.compiler.ast.ParentNode;

/**
 * An <code>ASTPattern</code> is either a TypePattern or a RecordPattern.
 * <em>
 * Pattern:
 * &nbsp;&nbsp;&nbsp;&nbsp;TypePattern
 * &nbsp;&nbsp;&nbsp;&nbsp;RecordPattern
 * </em>
 */
public sealed interface ASTPattern extends ParentNode permits ASTTypePattern, ASTRecordPattern {
}
