package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTConstructorDeclarator</code> is optionally Type Arguments,
 * then "constructor", followed by a pair of parentheses that may contain an
 * Formal Parameter List.</p>
 *
 * <em>
 * ConstructorDeclarator:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[TypeArguments] constructor ( [FormalParameterList] )
 * </em>
 */
public class ASTConstructorDeclarator extends ASTParentNode
{
    /**
     * Constructs an <code>ASTConstructorDeclarator</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTConstructorDeclarator(Location location, List<ASTNode> children)
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
