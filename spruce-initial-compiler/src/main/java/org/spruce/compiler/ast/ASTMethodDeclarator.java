package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTMethodDeclarator</code> is an identifier followed by an
 * optional formal parameter list within parentheses, optionally followed by a
 * ConstModifier.</p>
 *
 * <em>
 * MethodDeclarator:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier ( [FormalParameterList] ) [ConstModifier]
 * </em>
 */
public class ASTMethodDeclarator extends ASTParentNode
{
    /**
     * Constructs an <code>ASTMethodDeclarator</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTMethodDeclarator(Location location, List<ASTNode> children)
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
