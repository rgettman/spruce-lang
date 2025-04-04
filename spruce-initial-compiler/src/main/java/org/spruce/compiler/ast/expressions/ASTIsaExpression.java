package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.common.Location;

/**
 * An <code>ASTIsaExpression</code> is an Expression followed by "isa",
 * followed by an IsaTarget.
 * <em>
 * IsaExpression:
 * &nbsp;&nbsp;&nbsp;&nbsp;Expression isa IsaTarget
 * </em>
 */
public final class ASTIsaExpression extends ASTParentNode implements ASTValueExpression {
    private final ASTExpression myExpr;
    private final ASTIsaTarget myIsaTarget;

    /**
     * Constructs an <code>ASTCastExpression</code> at the given <code>Location</code>
     * with the given <code>ASTExpression</code> and the given <code>ASTIsaTarget</code>.
     * @param location The <code>Location</code>.
     * @param expr An <code>ASTExpression</code>.
     * @param isaTarget An <code>ASTIsaTarget</code>.
     */
    public ASTIsaExpression(Location location, ASTExpression expr, ASTIsaTarget isaTarget) {
        super(location);
        myExpr = expr;
        myIsaTarget = isaTarget;
    }

    /**
     * Returns an <code>ASTExpression</code>.
     * @return An <code>ASTExpression</code>.
     */
    public ASTExpression getExpr() {
        return myExpr;
    }

    /**
     * Returns an <code>ASTIsaTarget</code>.
     * @return An <code>ASTIsaTarget</code>.
     */
    public ASTIsaTarget getIsaTarget() {
        return myIsaTarget;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myExpr, myIsaTarget);
    }
}
