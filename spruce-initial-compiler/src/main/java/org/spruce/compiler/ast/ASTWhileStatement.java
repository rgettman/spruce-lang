package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTWhileStatement</code> is "while", optionally followed by an Init
 * within braces, followed by an expression (no incr/decr) in parentheses, and a statement.</p>
 *
 * <em>
 * WhileStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;while { Init } ( ExpressionNoIncrDecr ) Statement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;while ( ExpressionNoIncrDecr ) Statement
 * </em>
 */
public class ASTWhileStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTWhileStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTWhileStatement(Location location, List<ASTNode> children)
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
