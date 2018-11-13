package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTMethodBody</code> is a block or a semicolon.</p>
 *
 * <em>
 * MethodBody:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;;
 * </em>
 */
public class ASTMethodBody extends ASTParentNode
{
    /**
     * Constructs an <code>ASTMethodBody</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTMethodBody(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is NOT collapsible.
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return false;
    }
}
