package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTInterfaceMethodModifierList</code> is a list of general modifiers,
 * restricted to the following modifiers:</p>
 *
 * <ul>
 *     <li>abstract</li>
 *     <li>default</li>
 *     <li>override</li>
 *     <li>shared</li>
 *     <li>strictfp</li>
 * </ul>
 *
 * <em>
 * InterfaceMethodModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;GeneralModifier {GeneralModifier}
 * </em>
 */
public class ASTInterfaceMethodModifierList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTInterfaceMethodModifierList</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTInterfaceMethodModifierList(Location location, List<ASTNode> children)
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
