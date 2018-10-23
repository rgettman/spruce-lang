package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTMethodReference</code> is a reference to a method or constructor.
 * If [TypeArguments] Identifier is after "::", before the "::" can be an
 * Expression Name, a Primary, a DataType, "super", or TypeName "." super.  If
 * [TypeArguments] "new" is after "::", only a DataType is allowed before "::".</p>
 *
 * <em>
 * MethodReference:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;super :: [TypeArguments] Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName :: [TypeArguments] Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType :: [TypeArguments] Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType :: [TypeArguments] new<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary :: [TypeArguments] Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super :: [TypeArguments] Identifier
 * </em>
 */
public class ASTMethodReference extends ASTParentNode
{
    /**
     * Constructs an <code>ASTMethodReference</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTMethodReference(Location location, List<ASTNode> children)
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
