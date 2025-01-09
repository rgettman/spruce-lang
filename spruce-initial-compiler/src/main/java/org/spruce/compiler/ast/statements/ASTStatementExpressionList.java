package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTStatementExpressionList</code> is a comma-separated list of
 * statement expressions.</p>
 *
 * <em>
 * StatementExpressionList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpression {, StatementExpression}
 * </em>
 */
public final class ASTStatementExpressionList extends ASTListNode<ASTStatementExpression> implements ASTInit {
    /**
     * Constructs an <code>ASTStatementExpressionList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTStatementExpression</code>s.
     */
    public ASTStatementExpressionList(Location location, List<ASTStatementExpression> children) {
        super(location, children, Type.STMT_EXPRS);
    }
}
