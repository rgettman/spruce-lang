package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTBasicForStatement</code> is "for (", an optional Init, a
 * semicolon, an optional Expression (no incr/decr), another semicolon, an optional statement
 * expression list, ")", and a statement.</p>
 *
 * <em>
 * BasicForStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;for ( [Init] ; [ExpressionNoIncrDecr] ; [StatementExpressionList] ) Statement<br>
 * </em>
 */
public class ASTBasicForStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTBasicForStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTBasicForStatement(Location location, List<ASTNode> children)
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