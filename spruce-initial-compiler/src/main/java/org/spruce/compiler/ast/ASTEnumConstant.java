package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTEnumConstant</code> is an identifier with an optional argument list
 * within parentheses, optionally followed by a ClassBody.</p>
 *
 * <em>
 * EnumConstant:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier [( ArgumentList )] [ClassBody]
 * </em>
 */
public class ASTEnumConstant extends ASTParentNode
{
    /**
     * Constructs an <code>ASTEnumConstant</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTEnumConstant(Location location, List<ASTNode> children)
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
