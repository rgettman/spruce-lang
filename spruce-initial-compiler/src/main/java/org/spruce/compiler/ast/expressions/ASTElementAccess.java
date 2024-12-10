package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.statements.ASTResource;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTElementAccess</code> is a primary with an "index" expression
 * within brackets.</p>
 *
 * <p>The operators associated with element access expressions are left-associative.</p>
 *
 * <em>
 * ElementAccess:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary [ ValueExpression ]<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ElementAccess [ ValueExpression ]<br>
 * </em>
 */
public final class ASTElementAccess extends ASTParentNode implements ASTLeftHandSide, ASTResource {
    private final ASTPrimary myPrimary;
    private final ASTElementAccess myElementAccess;
    private final ASTValueExpression myIndexExpr;

    /**
     * Constructs an <code>ASTElementAccess</code> at the given <code>Location</code>
     * with the given <code>ASTPrimary</code> and the given <code>ASTValueExpression</code>.
     * representing the index Expression.
     * @param location The <code>Location</code>.
     * @param primary An <code>ASTPrimary</code>.
     * @param indexExpr An <code>ASTValueExpression</code> representing the index Expression.
     */
    public ASTElementAccess(Location location, ASTPrimary primary, ASTValueExpression indexExpr) {
        super(location);
        myPrimary = primary;
        myElementAccess = null;
        myIndexExpr = indexExpr;
    }

    /**
     * Constructs an <code>ASTElementAccess</code> at the given <code>Location</code>
     * with the given <code>ASTPrimary</code> and the given <code>ASTValueExpression</code>
     * representing the index Expression.
     * @param location The <code>Location</code>.
     * @param chain Another <code>ASTElementAccess</code> representing an earlier Element Access.
     * @param indexExpr An <code>ASTValueExpression</code> representing the index Expression.
     */
    public ASTElementAccess(Location location, ASTElementAccess chain, ASTValueExpression indexExpr) {
        super(location);
        myPrimary = null;
        myElementAccess = chain;
        myIndexExpr = indexExpr;
    }

    /**
     * Returns an <code>ASTPrimary</code>, if it exists.
     * @return An <code>Optional&lt;ASTPrimary&gt;</code>.
     */
    public Optional<ASTPrimary> getPrimary() {
        return Optional.ofNullable(myPrimary);
    }

    /**
     * Returns an <code>ASTElementAccess</code> representing the chained
     * ElementAccess, if it exists.
     * @return An <code>Optional&lt;ASTNode&gt;</code>.
     */
    public Optional<ASTElementAccess> getElementAccess() {
        return Optional.ofNullable(myElementAccess);
    }

    /**
     * Returns an <code>ASTValueExpression</code> representing the index Expression.
     * @return An <code>ASTValueExpression</code> representing the index Expression.
     */
    public ASTValueExpression getIndexExpr() {
        return myIndexExpr;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        if (myPrimary != null) {
            children.add(myPrimary);
        }
        else if (myElementAccess != null) {
            children.add(myElementAccess);
        }
        children.add(myIndexExpr);
        return children;
    }
}
