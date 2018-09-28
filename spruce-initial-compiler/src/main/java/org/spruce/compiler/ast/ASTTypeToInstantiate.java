package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeToInstantiate</code> is a TypeName optionally followed by
 * type arguments or diamond.
 *
 * <em>
 * TypeToInstantiate:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName TypeArgumentsOrDiamond
 * </em>
 */
public class ASTTypeToInstantiate extends ASTParentNode
{
    /**
     * Constructs an <code>ASTTypeToInstantiate</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTTypeToInstantiate(Location location, List<ASTNode> children)
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
