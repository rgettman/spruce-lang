package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTCatches</code> is a list of catch clauses.</p>
 *
 * <em>
 * Catches:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CatchClause {CatchClause}
 * </em>
 */
public class ASTCatches extends ASTListNode<ASTCatchClause> {
    /**
     * Constructs an <code>ASTCatches</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTCatchClause</code>s.
     */
    public ASTCatches(Location location, List<ASTCatchClause> children) {
        super(location, children, Type.CATCH_CLAUSES);
    }
}
