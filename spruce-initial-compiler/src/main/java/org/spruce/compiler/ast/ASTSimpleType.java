package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSimpleType</code> is a simple type with optional type arguments.</p>
 *
 * <em>
 * SimpleType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier TypeArguments
 * </em>
 */
public class ASTSimpleType extends ASTParentNode
{
    /**
     * Constructs an <code>ASTSimpleType</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSimpleType(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return false;
    }
}
