package org.spruce.compiler.bootstrap.ast.expressions;

import org.spruce.compiler.bootstrap.ast.ParentNode;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;

/**
 * An <code>ASTIsaTarget</code> is a Pattern or a DataType.
 * <em>
 * IsaTarget:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Pattern
 * </em>
 */
public sealed interface ASTIsaTarget extends ParentNode permits ASTDataType/*, ASTPattern*/ {
}
