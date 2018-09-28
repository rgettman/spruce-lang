package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTWildcard</code> is a "?" optionally followed by a
 * WildcardBounds.</p>
 *
 * <em>
 * WildCard:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;?<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;? WildcardBounds
 * </em>
 */
public class ASTWildcard extends ASTParentNode
{
    /**
     * Constructs an <code>ASTWildcard</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTWildcard(Location location, List<ASTNode> children)
    {
        super(location, children, TokenType.QUESTION_MARK);
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
