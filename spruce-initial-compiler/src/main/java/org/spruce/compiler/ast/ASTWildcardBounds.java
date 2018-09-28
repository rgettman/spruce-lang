package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTWildcardBounds</code> is a "&lt;:" or ":&gt;" optionally followed by a
 * DataType.</p>
 *
 * <em>
 * WildCard:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;: DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;:&gt; DataType
 * </em>
 */
public class ASTWildcardBounds extends ASTParentNode
{
    /**
     * Constructs an <code>ASTWildcardBounds</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTWildcardBounds(Location location, List<ASTNode> children)
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
