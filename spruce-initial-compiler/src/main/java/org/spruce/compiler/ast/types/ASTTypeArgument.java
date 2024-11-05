package org.spruce.compiler.ast.types;

import org.spruce.compiler.ast.ParentNode;

/**
 * <p>An <code>ASTTypeArgument</code> is a wildcard or a reference type.</p>
 *
 * <em>
 * TypeArgument:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Wildcard<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType
 * </em>
 */
public sealed interface ASTTypeArgument extends ParentNode permits ASTWildcard, ASTDataType {
}
