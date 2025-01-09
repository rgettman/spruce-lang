package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTForExpression</code> is a ForHeaderList followed by an Expression.</p>
 *
 * <em>
 * ForExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ForHeaderList Expression<br>
 * </em>
 */
public final class ASTForExpression extends ASTParentNode implements ASTValueExpression {
    private final ASTForHeaderList myForHeaders;
    private final ASTExpression myExpr;

    /**
     * Constructs an <code>ASTForExpression</code> at the given <code>Location</code>
     * with the given <code>ASTForHeaderList</code> and the given
     * <code>ASTExpression</code> representing the element value.
     * @param location The <code>Location</code>.
     * @param forHeaders An <code>ASTForHeaderList</code>.
     * @param expr An <code>ASTExpression</code> representing the element value.
     */
    public ASTForExpression(Location location, ASTForHeaderList forHeaders, ASTExpression expr) {
        super(location);
        myForHeaders = forHeaders;
        myExpr = expr;
    }

    /**
     * Returns an <code>ASTForHeaderList</code>.
     * @return An <code>ASTForHeaderList</code>.
     */
    public ASTForHeaderList getForHeaderList() {
        return myForHeaders;
    }

    /**
     * Returns an <code>ASTExpression</code> representing the element value.
     * @return An <code>ASTExpression</code> representing the element value.
     */
    public ASTExpression getExpr() {
        return myExpr;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myForHeaders, myExpr);
    }
}
