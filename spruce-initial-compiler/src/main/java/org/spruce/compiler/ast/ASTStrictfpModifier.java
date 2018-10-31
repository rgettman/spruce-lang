package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTStrictfpModifier</code> is "strictfp".</p>
 *
 * <em>
 * StrictfpModifier:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;strictfp
 * </em>
 */
public class ASTStrictfpModifier extends ASTParentNode
{
    /**
     * Constructs an <code>ASTStrictfpModifier</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTStrictfpModifier(Location location, List<ASTNode> children)
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
