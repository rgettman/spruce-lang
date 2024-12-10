package org.spruce.compiler.ast.types;

import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.names.ASTExpressionName;

/**
 * <p>An <code>ASTDataType</code> is a data type (no array) or an array type.</p>
 *
 * <em>
 * DataType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ArrayType
 * </em>
 */
public sealed interface ASTDataType extends ParentNode, ASTTypeArgument permits ASTDataTypeNoArray, ASTArrayType {
    /**
     * Returns whether this DataType can be converted to an ExpressionName.
     * @return Whether this DataType can be converted to an ExpressionName.
     */
    boolean canConvertToExpressionName();

    /**
     * Converts this DataType into an Expression Name.
     * @return An <code>ASTExpressionName</code>.
     */
    ASTExpressionName convertToExpressionName();
}
