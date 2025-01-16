package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTExpression;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTReturnStatement</code> is "return" optionally followed by an
 * expression, then a semicolon.</p>
 *
 * <em>
 * ReturnStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;return ;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;return Expression ;
 * </em>
 */
public final class ASTReturnStatement extends ASTParentNode implements ASTStatement {
    private final ASTExpression myExpr;

    /**
     * Constructs an <code>ASTReturnStatement</code> at the given <code>Location</code>
     * with the given <code>ASTExpression</code>.
     * @param location The <code>Location</code>.
     * @param expr An <code>ASTExpression</code>.
     */
    public ASTReturnStatement(Location location, ASTExpression expr) {
        super(location);
        myExpr = expr;
    }

    /**
     * Constructs an <code>ASTReturnStatement</code> at the given <code>Location</code>
     * with no Expression.
     * @param location The <code>Location</code>.
     */
    public ASTReturnStatement(Location location) {
        super(location);
        myExpr = null;
    }

    /**
     * Returns an <code>ASTExpression</code>, if it exists.
     * @return An <code>Optional&lt;ASTExpression&gt;</code>.
     */
    public Optional<ASTExpression> getExpr() {
        return Optional.ofNullable(myExpr);
    }

    @Override
    public List<Node> getChildren() {
        return myExpr != null ? Arrays.asList(myExpr) : Collections.emptyList();
    }
}
