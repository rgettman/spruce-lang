package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.types.ASTIntersectionType;
import org.spruce.compiler.scanner.Location;

/**
 * An <code>ASTCastExpression</code> is an Expression followed by "as",
 * followed by an IntersectionType.
 * <em>
 * CastExpression:
 * &nbsp;&nbsp;&nbsp;&nbsp;Expression as IntersectionType
 * </em>
 */
public final class ASTCastExpression extends ASTParentNode implements ASTValueExpression {
    private final ASTExpression myExpr;
    private final ASTIntersectionType myIntersectionType;

    /**
     * Constructs an <code>ASTCastExpression</code> at the given <code>Location</code>
     * with the given <code>ASTExpression</code> and the given
     * <code>ASTIntersectionType</code>.
     * @param location The <code>Location</code>.
     * @param expr An <code>ASTExpression</code>.
     * @param intersectionType An <code>ASTIntersectionType</code>.
     */
    public ASTCastExpression(Location location, ASTExpression expr, ASTIntersectionType intersectionType) {
        super(location);
        myExpr = expr;
        myIntersectionType = intersectionType;
    }

    /**
     * Returns an <code>ASTExpression</code>.
     * @return An <code>ASTExpression</code>.
     */
    public ASTExpression getExpr() {
        return myExpr;
    }

    /**
     * Returns an <code>ASTIntersectionType</code>.
     * @return An <code>ASTIntersectionType</code>.
     */
    public ASTIntersectionType getIntersectionType() {
        return myIntersectionType;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myExpr, myIntersectionType);
    }
}
