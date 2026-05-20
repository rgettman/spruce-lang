package org.spruce.compiler.bootstrap.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

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
    private TypeSymbol myResolvedDataType;

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
    public void setResolvedDataType(TypeSymbol symbol) {
        myResolvedDataType = symbol;
    }

    @Override
    public TypeSymbol getResolvedDataType() {
        return myResolvedDataType;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myExpr, myIsaTarget);
    }
}
