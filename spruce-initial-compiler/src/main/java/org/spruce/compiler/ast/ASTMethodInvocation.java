package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTMethodInvocation</code> is a primary with an argument list
 * within parentheses.</p>
 *
 * <em>
 * MethodInvocation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary ( ArgumentList )
 * </em>
 */
public class ASTMethodInvocation extends ASTParentNode
{
    /**
     * Constructs an <code>ASTElementAccess</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTMethodInvocation(Location location, List<ASTNode> children)
    {
        super(location, children, TokenType.OPEN_PARENTHESIS);
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
