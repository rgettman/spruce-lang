package org.spruce.compiler.ast.types;

import org.spruce.compiler.ast.ParentNode;

/**
 * <p>An <code>ASTTypeArgument</code> is a wildcard "_" or a TypeArgumentBounds.</p>
 *
 * <em>
 * TypeArgument:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Wildcard<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeArgumentBounds
 * </em>
 */
public sealed interface ASTTypeArgument extends ParentNode permits ASTWildcard, ASTTypeArgumentBounds {
}
