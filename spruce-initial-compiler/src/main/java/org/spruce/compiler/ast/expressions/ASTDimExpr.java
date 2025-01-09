package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDimExpr</code> is a value expression inside brackets.</p>
 *
 * <em>
 * DimExpr:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[ ValueExpression ]
 * </em>
 */
public class ASTDimExpr extends ASTParentNode {
    private final ASTValueExpression myValueExpr;

    /**
     * Constructs an <code>ASTDimExpr</code> at the given <code>Location</code>
     * with the given <code>ASTValueExpression</code> representing the dimensional size.
     * @param location The <code>Location</code>.
     * @param valueExpr An <code>ASTValueExpression</code> representing the dimensional size.
     */
    public ASTDimExpr(Location location, ASTValueExpression valueExpr) {
        super(location);
        myValueExpr = valueExpr;
    }

    /**
     * Returns an <code>ASTValueExpression</code> representing the dimensional size.
     * @return An <code>ASTValueExpression</code> representing the dimensional size.
     */
    public ASTValueExpression getValueExpr() {
        return myValueExpr;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myValueExpr);
    }
}
