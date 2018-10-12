package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDoStatement</code> is "do", followed by a Statement, "while ",
 * an expression (no incr/decr) in parentheses, and a semicolon.</p>
 *
 * <em>
 * DoStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;do Statement while ( ExpressionNoIncrDecr ) ;
 * </em>
 */
public class ASTDoStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTDoStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTDoStatement(Location location, List<ASTNode> children)
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
