package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSynchronizedStatement</code> is "synchronized", followed by
 * an expression (no incr/decr) in parentheses, and a block.</p>
 *
 * <em>
 * SynchronizedStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;synchronized ( ExpressionNoIncrDecr ) Block
 * </em>
 */
public class ASTSynchronizedStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTSynchronizedStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSynchronizedStatement(Location location, List<ASTNode> children)
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
