package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTArrayInitializer</code> is a variable initializer list within
 * braces.</p>
 *
 * <em>
 * ArrayInitializer:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ VariableInitializerList }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ }
 * </em>
 */
public class ASTArrayInitializer extends ASTParentNode
{
    /**
     * Constructs an <code>ASTArrayInitializer</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTArrayInitializer(Location location, List<ASTNode> children)
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
