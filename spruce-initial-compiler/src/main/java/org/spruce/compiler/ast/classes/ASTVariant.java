package org.spruce.compiler.ast.classes;

import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.types.ASTDataType;

/**
 * <p>An <code>ASTVariant</code> is a variant type or a compact record declaration.</p>
 *
 * <em>
 * Variant:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariantType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CompactRecordDeclaration<br>
 * </em>
 */
public sealed interface ASTVariant extends ParentNode permits ASTVariantType, ASTCompactRecordDeclaration {
}
