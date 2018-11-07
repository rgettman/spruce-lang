package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTEnumBodyDeclarations</code> is a semicolon followed by a
 * class part list.</p>
 *
 * <em>
 * EnumBodyDeclarations:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;; ClassPartList
 * </em>
 */
public class ASTEnumBodyDeclarations extends ASTParentNode
{
    /**
     * Constructs an <code>ASTEnumBodyDeclarations</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTEnumBodyDeclarations(Location location, List<ASTNode> children)
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
