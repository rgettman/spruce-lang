package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLeftHandSide</code> is an expression suitable for the left-
 * hand side of an assignment expression.</p>
 *
 * <em>
 * LeftHandSide:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ElementAccess<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FieldAccess
 * </em>
 */
public class ASTLeftHandSide extends ASTParentNode {
    private final ASTExpressionName myExprName;
    private final ASTElementAccess myElementAccess;
    private final ASTFieldAccess myFieldAccess;

    /**
     * Constructs an <code>ASTLeftHandSide</code> at the given <code>Location</code>
     * with the given <code>ASTExpressionName</code>.
     * @param location The <code>Location</code>.
     * @param exprName An <code>ASTExpressionName</code>.
     */
    public ASTLeftHandSide(Location location, ASTExpressionName exprName) {
        super(location);
        myExprName = exprName;
        myElementAccess = null;
        myFieldAccess = null;
    }

    /**
     * Constructs an <code>ASTLeftHandSide</code> at the given <code>Location</code>
     * with the given <code>ASTElementAccess</code>.
     * @param location The <code>Location</code>.
     * @param elementAccess An <code>ASTElementAccess</code>.
     */
    public ASTLeftHandSide(Location location, ASTElementAccess elementAccess) {
        super(location);
        myExprName = null;
        myElementAccess = elementAccess;
        myFieldAccess = null;
    }

    /**
     * Constructs an <code>ASTLeftHandSide</code> at the given <code>Location</code>
     * with the given <code>ASTFieldAccess</code>.
     * @param location The <code>Location</code>.
     * @param fieldAccess An <code>ASTFieldAccess</code>.
     */
    public ASTLeftHandSide(Location location, ASTFieldAccess fieldAccess) {
        super(location);
        myExprName = null;
        myElementAccess = null;
        myFieldAccess = fieldAccess;
    }

    /**
     * Returns an <code>ASTExpressionName</code>, if it exists.
     * @return An <code>Optional&lt;ASTExpressionName&gt;</code>.
     */
    public Optional<ASTExpressionName> getExprName() {
        return Optional.ofNullable(myExprName);
    }

    /**
     * Returns an <code>ASTElementAccess</code>, if it exists.
     * @return An <code>Optional&lt;ASTElementAccess&gt;</code>.
     */
    public Optional<ASTElementAccess> getElementAccess() {
        return Optional.ofNullable(myElementAccess);
    }

    /**
     * Returns an <code>ASTFieldAccess</code>, if it exists.
     * @return An <code>Optional&lt;ASTFieldAccess&gt;</code>.
     */
    public Optional<ASTFieldAccess> getFieldAccess() {
        return Optional.ofNullable(myFieldAccess);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(1);
        if (myExprName != null) {
            children.add(myExprName);
        }
        else if (myElementAccess != null) {
            children.add(myElementAccess);
        }
        else {
            children.add(myFieldAccess);
        }
        return children;
    }
}
