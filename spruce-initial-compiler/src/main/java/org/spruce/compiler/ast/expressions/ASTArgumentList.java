package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTArgumentList</code> is a comma-separated list of expressions.</p>
 *
 * <em>
 * ArgumentList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ValueExpression {, ValueExpression}
 * </em>
 */
public class ASTArgumentList extends ASTListNode<ASTGiveExpression> {
    /**
     * Constructs an <code>ASTArgumentList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTGiveExpression</code>s.
     */
    public ASTArgumentList(Location location, List<ASTGiveExpression> children) {
        super(location, children, Type.ARGUMENTS);
    }
}
