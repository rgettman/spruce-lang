package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDimExprs</code> is a list of dim expr instances.</p>
 *
 * <em>
 * DimExprs:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DimExpr {DimExpr}<br>
 * </em>
 */
public class ASTDimExprs extends ASTListNode<ASTDimExpr> {
    /**
     * Constructs an <code>ASTListNode</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTDimExpr</code>s.
     */
    public ASTDimExprs(Location location, List<ASTDimExpr> children) {
        super(location, children, Type.DIM_EXPRS);
    }
}
