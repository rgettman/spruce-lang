package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCatchClause</code> is "catch" followed by a CatchFormalParameter
 * within parentheses, and a block.</p>
 *
 * <em>
 * CatchClause:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;catch ( CatchFormalParameter ) Block
 * </em>
 */
public class ASTCatchClause extends ASTParentNode
{
    /**
     * Constructs an <code>ASTCatchClause</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTCatchClause(Location location, List<ASTNode> children)
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
