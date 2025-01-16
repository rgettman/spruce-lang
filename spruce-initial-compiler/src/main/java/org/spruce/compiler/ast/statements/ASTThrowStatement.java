package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTValueExpression;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTThrowStatement</code> is "throw" followed by a value
 * expression, then a semicolon.</p>
 *
 * <em>
 * ThrowStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;throw ValueExpression ;
 * </em>
 */
public final class ASTThrowStatement extends ASTParentNode implements ASTStatement {
    private final ASTValueExpression myValueExpr;

    /**
     * Constructs an <code>ASTThrowStatement</code> at the given <code>Location</code>
     * with the given <code>ASTValueExpression</code>.
     * @param location The <code>Location</code>.
     * @param valueExpr An <code>ASTValueExpression</code>.
     */
    public ASTThrowStatement(Location location, ASTValueExpression valueExpr) {
        super(location);
        myValueExpr = valueExpr;
    }

    /**
     * Returns an <code>ASTValueExpression</code>.
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression getValueExpr() {
        return myValueExpr;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myValueExpr);
    }
}
