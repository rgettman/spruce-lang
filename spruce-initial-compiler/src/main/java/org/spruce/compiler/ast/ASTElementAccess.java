package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTElementAccess</code> is a primary with an "index" expression
 * within brackets.</p>
 *
 * <p>The operators associated with element access expressions are left-associative.</p>
 *
 * <em>
 * ElementAccess:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary [ Expression ]<br>
 * </em>
 */
public class ASTElementAccess extends ASTParentNode
{
    /**
     * Constructs an <code>ASTElementAccess</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTElementAccess(Location location, List<ASTNode> children)
    {
        super(location, children, TokenType.OPEN_BRACKET);
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
