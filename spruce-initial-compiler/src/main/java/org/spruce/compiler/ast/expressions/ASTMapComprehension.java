package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTMapComprehension</code> is a "for" expression surrounded by
 * braces.</p>
 *
 * <em>
 * MapComprehension:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ ForExpression }
 * </em>
 */
public final class ASTMapComprehension extends ASTParentNode {
    private final ASTForExpression myForExpr;

    /**
     * Constructs an <code>ASTMapComprehension</code> at the given <code>Location</code>
     * with an <code>ASTForExpression</code> as its child.
     * @param location The <code>Location</code>.
     * @param forExpr An <code>ASTForExpression</code>.
     */
    public ASTMapComprehension(Location location, ASTForExpression forExpr) {
        super(location);
        myForExpr = forExpr;
    }

    /**
     * Returns an <code>ASTForExpression</code>.
     * @return An <code>ASTForExpression</code>.
     */
    public ASTForExpression getForExpr() {
        return myForExpr;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myForExpr);
    }
}
