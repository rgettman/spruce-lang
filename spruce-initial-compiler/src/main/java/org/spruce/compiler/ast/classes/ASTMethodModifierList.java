package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTMethodModifierList</code> is a list of general modifiers,
 * restricted to the following modifiers:</p>
 *
 * <ul>
 *     <li>abstract</li>
 *     <li>final</li>
 *     <li>override</li>
 *     <li>shared</li>
 * </ul>
 *
 * <em>
 * MethodModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;GeneralModifier {GeneralModifier}
 * </em>
 */
public class ASTMethodModifierList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTMethodModifierList</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTMethodModifierList(Location location, List<ASTNode> children)
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
