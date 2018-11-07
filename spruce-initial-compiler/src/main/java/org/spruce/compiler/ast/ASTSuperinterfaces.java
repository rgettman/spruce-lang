package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSuperinterfaces</code> is "implements" followed by a Data Type No Array List.</p>
 *
 * <em>
 * Superinterfaces:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;implements DataTypeNoArrayList
 * </em>
 */
public class ASTSuperinterfaces extends ASTParentNode
{
    /**
     * Constructs an <code>ASTSuperinterfaces</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTSuperinterfaces(Location location, List<ASTNode> children)
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
