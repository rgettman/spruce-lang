package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTInterfaceModifierList</code> is a list of general modifiers,
 * restricted to the following modifiers:</p>
 *
 * <ul>
 *     <li>abstract</li>
 *     <li>shared</li>
 *     <li>strictfp</li>
 * </ul>
 *
 * <em>
 * InterfaceModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;GeneralModifier {GeneralModifier}
 * </em>
 */
public class ASTInterfaceModifierList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTClassModifierList</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTInterfaceModifierList(Location location, List<ASTNode> children)
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
