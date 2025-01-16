package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTGuard</code> is "when" followed by a conditional expression.</p>
 *
 * <em>
 * Guard:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;when ValueExpression<br>
 * </em>
 */
public class ASTGuard extends ASTParentNode {
    private final ASTValueExpression myExpr;

    /**
     * Constructs an <code>ASTGuard</code> at the given <code>Location</code>
     * with the given <code>ASTValueExpression</code>.
     * @param location The <code>Location</code>.
     * @param expr An <code>ASTValueExpression</code>.
     */
    public ASTGuard(Location location, ASTValueExpression expr) {
        super(location);
        myExpr = expr;
    }

    /**
     * Returns an <code>ASTValueExpression</code>.
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression getExpr() {
        return myExpr;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myExpr);
    }
}
