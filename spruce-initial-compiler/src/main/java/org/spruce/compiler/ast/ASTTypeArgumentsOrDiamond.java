package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTTypeArgumentsOrDiamond</code> is a TypeArguments or "&lt;&gt;".
 *
 * <p>To distinguish otherwise ambiguous parsings, parsing of this node will
 * turn on the type context in the Scanner for the duration of this parsing.</p>
 *
 * <em>
 * TypeArgumentsOrDiamond:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeArguments<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt; &gt;
 * </em>
 */
public class ASTTypeArgumentsOrDiamond extends ASTParentNode
{
    /**
     * Constructs an <code>ASTTypeArgumentsOrDiamond</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTTypeArgumentsOrDiamond(Location location, List<ASTNode> children)
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
