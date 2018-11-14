package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTElementValueArrayInitializer</code> is an optional element
 * value list within braces.</p>
 *
 * <em>
 * ElementValueArrayInitializer:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ ElementValueList }
 * </em>
 */
public class ASTElementValueArrayInitializer extends ASTParentNode
{
    /**
     * Constructs an <code>ASTElementValueArrayInitializer</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTElementValueArrayInitializer(Location location, List<ASTNode> children)
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
