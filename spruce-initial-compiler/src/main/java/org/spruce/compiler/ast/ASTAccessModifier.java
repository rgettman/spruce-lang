package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAccessModifier</code> is "public", "protected", "internal",
 * "private".</p>
 *
 * <p>Other modifiers are in productions such as MethodModifier, ConstModifier,
 * ClassModifier, InterfaceModifier.</p>
 *
 * <em>
 * MethodModifier:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;public<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;protected<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;internal<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;private
 * </em>
 */
public class ASTAccessModifier extends ASTParentNode
{
    /**
     * Constructs an <code>ASTAccessModifier</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTAccessModifier(Location location, List<ASTNode> children)
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
