package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTExpression;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTYieldStatement</code> is "yield" followed by an
 * expression, then a semicolon.</p>
 *
 * <em>
 * YieldStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;yield Expression ;
 * </em>
 */
public final class ASTYieldStatement extends ASTParentNode implements ASTStatement {
    private final ASTExpression myExpr;

    /**
     * Constructs an <code>ASTYieldStatement</code> at the given <code>Location</code>
     * given the <code>ASTExpression</code>.
     * @param location The <code>Location</code>.
     * @param expr <code>ASTExpression</code>.
     */
    public ASTYieldStatement(Location location, ASTExpression expr) {
        super(location);
        myExpr = expr;
    }

    /**
     * Returns an <code>ASTExpression</code>.
     * @return An <code>ASTExpression</code>.
     */
    public ASTExpression getExpr() {
        return myExpr;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myExpr);
    }
}
