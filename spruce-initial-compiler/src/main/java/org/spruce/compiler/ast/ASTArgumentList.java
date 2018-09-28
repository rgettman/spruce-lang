package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTArgumentList</code> is a comma-separated list of expressions.
 *
 * <p>The operators associated with argument lists are left-associative.</p>
 *
 * <em>
 * ArgumentList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Expression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ArgumentList , Expression<br>
 * </em>
 */
public class ASTArgumentList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTArgumentList</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTArgumentList(Location location, List<ASTNode> children)
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
