package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTClassLiteral</code> is a type name, possibly multiple "[]",
 * followed by "." and "class".</p>
 *
 * <p>
 *     TODO: When annotations are introduced, make sure they aren't allowed
 *     when parsing "Dims" here.
 * </p>
 *
 * <em>
 * ClassLiteral:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . class<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName Dims . class
 * </em>
 */
public class ASTClassLiteral extends ASTParentNode
{
    /**
     * Constructs an <code>ASTClassLiteral</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTClassLiteral(Location location, List<ASTNode> children)
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
