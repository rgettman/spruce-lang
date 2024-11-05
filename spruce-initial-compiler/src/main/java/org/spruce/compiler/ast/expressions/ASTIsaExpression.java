package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.scanner.Location;

/**
 * An <code>ASTIsaExpression</code> is an Expression followed by "isa",
 * followed by a DataType.
 * <em>
 * IsaExpression:
 * &nbsp;&nbsp;&nbsp;&nbsp;Expression isa DataType
 * </em>
 */
public final class ASTIsaExpression extends ASTParentNode implements ASTValueExpression {
    private final ASTExpression myExpr;
    private final ASTDataType myDataType;

    /**
     * Constructs an <code>ASTCastExpression</code> at the given <code>Location</code>
     * with the given <code>ASTExpression</code> and the given <code>ASTListNode</code>
     * representing an IntersectionType.
     * @param location The <code>Location</code>.
     * @param expr An <code>ASTExpression</code>.
     * @param dataType An <code>ASTDataType</code>.
     */
    public ASTIsaExpression(Location location, ASTExpression expr, ASTDataType dataType) {
        super(location);
        myExpr = expr;
        myDataType = dataType;
    }

    /**
     * Returns an <code>ASTExpression</code>.
     * @return An <code>ASTExpression</code>.
     */
    public ASTExpression getExpr() {
        return myExpr;
    }

    /**
     * Returns an <code>ASTDataType</code>.
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType getIntersectionType() {
        return myDataType;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myExpr, myDataType);
    }
}
