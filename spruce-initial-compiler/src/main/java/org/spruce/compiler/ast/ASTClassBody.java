package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTClassBody</code> is a "{", optionally a list of class parts,
 * followed by a "}".</p>
 *
 * <em>
 * ClassBody:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ ClassPartList }
 * </em>
 */
public class ASTClassBody extends ASTParentNode
{
    /**
     * Constructs an <code>ASTClassBody</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTClassBody(Location location, List<ASTNode> children)
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
