package org.spruce.compiler.bootstrap.ast.types;

import org.spruce.compiler.bootstrap.ast.ParentNode;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;

/**
 * <p>An <code>ASTBaseDataType</code> is a data type (no array) or an array type.</p>
 *
 * <em>
 * BaseDataType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ArrayType
 * </em>
 */
public sealed interface ASTBaseDataType extends ParentNode permits ASTDataTypeNoArray/*, ASTArrayType*/ {
    /**
     * Returns whether this BaseDataType can be converted to an ExpressionName.
     * @return Whether this BaseDataType can be converted to an ExpressionName.
     */
    boolean canConvertToExpressionName();

    /**
     * Converts this BaseDataType into an Expression Name.
     * @return An <code>ASTExpressionName</code>.
     */
    ASTExpressionName convertToExpressionName();
}
