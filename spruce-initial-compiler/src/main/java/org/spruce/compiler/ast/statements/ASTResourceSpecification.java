package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTResourceSpecification</code> is a resource list, optionally
 * followed by a semicolon, all within parentheses.</p>
 *
 * <em>
 * ResourceSpecification:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;( ResourceList )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;( ResourceList ; )
 * </em>
 */
public class ASTResourceSpecification extends ASTParentNode
{
    /**
     * Constructs an <code>ASTResourceSpecification</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTResourceSpecification(Location location, List<ASTNode> children)
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
