package org.spruce.compiler.bootstrap.ast.types;

import org.spruce.compiler.bootstrap.ast.ParentNode;
import org.spruce.compiler.bootstrap.ast.SymbolReference;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;

/**
 * <p>An <code>ASTBaseDataType</code> is a data type (no array).</p>
 *
 * <em>
 * BaseDataType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray
 * </em>
 */
public sealed interface ASTBaseDataType extends ParentNode, SymbolReference<ParentSymbol> permits ASTDataTypeNoArray {
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

    /**
     * Returns a normalized String representation of this base data type.
     * @return A normalized String representation.
     */
    String getTypeName();
}
