package org.spruce.compiler.ast.types;

import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.classes.ASTVariant;

/**
 * <p>An <code>ASTDataType</code> is a data type (no array) or an array type.</p>
 *
 * <em>
 * DataType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ArrayType
 * </em>
 */
public sealed interface ASTDataType extends ParentNode, ASTTypeArgument, ASTVariant permits ASTDataTypeNoArray, ASTArrayType {
}
