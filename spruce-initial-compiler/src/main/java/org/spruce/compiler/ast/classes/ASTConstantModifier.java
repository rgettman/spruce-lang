package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTConstantModifier</code> is "constant".</p>
 *
 * <em>
 * ConstantModifier:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;constant
 * </em>
 */
public class ASTConstantModifier extends ASTParentNode
{
    /**
     * Constructs an <code>ASTConstantModifier</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTConstantModifier(Location location, List<ASTNode> children)
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
