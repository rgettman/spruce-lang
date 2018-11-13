package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
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
public class ASTStatementExpressionList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTStatementExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTStatementExpressionList(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return true;
    }
}
