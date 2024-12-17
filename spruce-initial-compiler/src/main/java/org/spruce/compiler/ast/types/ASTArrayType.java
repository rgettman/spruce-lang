package org.spruce.compiler.ast.types;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTArrayType</code> is a data type (no array) with dimensions.</p>
 *
 * <em>
 * ArrayType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray Dims
 * </em>
 */
public final class ASTArrayType extends ASTParentNode implements ASTBaseDataType {
    private final ASTDataTypeNoArray myDataTypeNoArray;
    private final ASTDims myDims;

    /**
     * Constructs an <code>ASTArrayType</code> at the given <code>Location</code>
     * with the given <code>ASTDataTypeNoArray</code> and the given <code>ASTDims</code>.
     * @param location The <code>Location</code>.
     * @param dataTypeNoArray An <code>ASTDataTypeNoArray</code>.
     * @param dims An <code>ASTDims</code>.
     */
    public ASTArrayType(Location location, ASTDataTypeNoArray dataTypeNoArray, ASTDims dims) {
        super(location);
        myDataTypeNoArray = dataTypeNoArray;
        myDims = dims;
    }

    /**
     * Returns an <code>ASTDataTypeNoArray</code>.
     * @return An <code>ASTDataTypeNoArray</code>.
     */
    public ASTDataTypeNoArray getDataTypeNoArray() {
        return myDataTypeNoArray;
    }

    /**
     * Returns an <code>ASTDims</code>.
     * @return An <code>ASTDims</code>.
     */
    public ASTDims getDims() {
        return myDims;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myDataTypeNoArray, myDims);
    }

    /**
     * An ArrayType cannot be converted to an ExpressionName.
     * @return <code>false</code>.
     */
    @Override
    public boolean canConvertToExpressionName() {
        return false;
    }

    /**
     * An Array type does not convert into an Expression Name.
     * @return An <code>ASTExpressionName</code>.
     */
    @Override
    public ASTExpressionName convertToExpressionName() {
        throw new IllegalStateException("Internal error: Expected a variable, got an array type!");
    }
}
