package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTExtendsInterfaces</code> is "extends" followed by a Data Type No Array List.</p>
 *
 * <em>
 * ExtendsInterfaces:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;extends DataTypeNoArrayList
 * </em>
 */
public class ASTExtendsInterfaces extends ASTParentNode
{
    /**
     * Constructs an <code>ASTExtendsInterfaces</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTExtendsInterfaces(Location location, List<ASTNode> children)
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
