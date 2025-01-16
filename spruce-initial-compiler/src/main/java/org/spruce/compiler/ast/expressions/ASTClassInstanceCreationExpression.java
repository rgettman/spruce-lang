package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.statements.ASTStatementExpression;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTClassInstanceCreationExpression</code> is an unqualified
 * class instance creation expression that may be preceded by a primary and ".".</p>
 *
 * <em>
 * ClassInstanceCreationExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UnqualifiedClassInstanceCreationExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary . UnqualifiedClassInstanceCreationExpression
 * </em>
 */
public final class ASTClassInstanceCreationExpression extends ASTParentNode implements ASTStatementExpression {
    private final ASTPrimary myPrimary;
    private final ASTUnqualifiedClassInstanceCreationExpression myUcice;

    /**
     * Constructs an <code>ASTClassInstanceCreationExpression</code> at the given <code>Location</code>
     * with the given <code>ASTPrimary</code> and the given
     * <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     * @param location The <code>Location</code>.
     * @param primary An <code>ASTPrimary</code>.
     * @param ucice An <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     */
    public ASTClassInstanceCreationExpression(Location location, ASTPrimary primary,
                                              ASTUnqualifiedClassInstanceCreationExpression ucice) {
        super(location);
        myPrimary = primary;
        myUcice = ucice;
    }

    /**
     * Constructs an <code>ASTClassInstanceCreationExpression</code> at the given <code>Location</code>
     * with the given <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     * @param location The <code>Location</code>.
     * @param ucice An <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     */
    public ASTClassInstanceCreationExpression(Location location,
                                              ASTUnqualifiedClassInstanceCreationExpression ucice) {
        super(location);
        myPrimary = null;
        myUcice = ucice;
    }

    /**
     * Returns an <code>ASTPrimary</code>, if it exists.
     * @return An <code>Optional&lt;Primary&gt;</code>.
     */
    public Optional<ASTPrimary> getPrimary() {
        return Optional.ofNullable(myPrimary);
    }

    /**
     * Returns an <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     * @return An <code>ASTUnqualifiedClassInstanceCreationExpression</code>.
     */
    public ASTUnqualifiedClassInstanceCreationExpression getUcice() {
        return myUcice;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        if (myPrimary != null) {
            children.add(myPrimary);
        }
        children.add(myUcice);
        return children;
    }
}
