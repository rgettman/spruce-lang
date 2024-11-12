package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.classes.ASTVariant;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.exception.CompileException;

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
    /**
     * Converts a Data Type into an Expression Name.
     * @return An <code>ASTExpressionName</code>.
     */
    ASTExpressionName convertToExpressionName();
}
