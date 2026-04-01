package org.spruce.compiler.bootstrap.ast.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.expressions.ASTIsaTarget;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTDataType</code> is a base data type optionally followed by a
 * '?' or a '!'.</p>
 *
 * <em>
 * DataType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BaseDataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BaseDataType !<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BaseDataType ?
 * </em>
 */
public final class ASTDataType extends ASTParentNode implements ASTIsaTarget {
    private final ASTBaseDataType myBaseDataType;
    private final ASTKeywordNode mySuffixOperator;

    /**
     * Constructs an <code>ASTDataType</code> at the given <code>Location</code>
     * with the given <code>ASTBaseDataType</code> and the given <code>ASTKeywordNode</code>
     * representing the suffix operator.
     * @param location The <code>Location</code>.
     * @param baseDataType An <code>ASTBaseDataType</code>.
     * @param suffixOperator An <code>ASTKeywordNode</code> representing the suffix operator.
     */
    public ASTDataType(Location location, ASTBaseDataType baseDataType, ASTKeywordNode suffixOperator) {
        super(location);
        myBaseDataType = baseDataType;
        mySuffixOperator = suffixOperator;
    }

    /**
     * Constructs an <code>ASTDataType</code> at the given <code>Location</code>
     * with the given <code>ASTBaseDataType</code> and the given <code>ASTKeywordNode</code>
     * representing the suffix operator.
     * @param location The <code>Location</code>.
     * @param baseDataType An <code>ASTBaseDataType</code>.
     */
    public ASTDataType(Location location, ASTBaseDataType baseDataType) {
        super(location);
        myBaseDataType = baseDataType;
        mySuffixOperator = null;
    }

    /**
     * Returns the <code>ASTBaseDataType</code>.
     * @return The <code>ASTBaseDataType</code>.
     */
    public ASTBaseDataType getBaseDataType() {
        return myBaseDataType;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the suffix operator, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    public Optional<ASTKeywordNode> getSuffixOperator() {
        return Optional.ofNullable(mySuffixOperator);
    }

    /**
     * Returns a normalized String representation of this data type.  It
     * consists of the base data type's normalized String representation, with
     * the suffix operator appended if it exists.
     * @return A normalized String representation of this data type.
     */
    public String getTypeName() {
        StringBuilder buf = new StringBuilder(myBaseDataType.getTypeName());
        if (mySuffixOperator != null) {
            buf.append(mySuffixOperator.getKeyword().getRepresentation());
        }
        return buf.toString();
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myBaseDataType);
        if (mySuffixOperator != null) {
            children.add(mySuffixOperator);
        }
        return children;
    }

    /**
     * Returns whether this DataType can be converted to an ExpressionName.
     * @return Whether this DataType can be converted to an ExpressionName.
     */
    public boolean canConvertToExpressionName() {
        return mySuffixOperator == null && myBaseDataType.canConvertToExpressionName();
    }

    /**
     * Converts this DataType into an Expression Name.
     * @return An <code>ASTExpressionName</code>.
     */
    public ASTExpressionName convertToExpressionName() {
        if (mySuffixOperator != null) {
            throw new IllegalStateException("Internal error: Expected a variable, got a data type with operator '" +
                    mySuffixOperator.getKeyword().getRepresentation() + "'!");
        }
        return myBaseDataType.convertToExpressionName();
    }
}
