package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTInterfaceModifierList</code> is a list of general modifiers,
 * restricted to the following modifiers:</p>
 *
 * <ul>
 *     <li>abstract</li>
 *     <li>shared</li>
 *     <li>sealed</li>
 * </ul>
 *
 * <em>
 * InterfaceModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;GeneralModifier {GeneralModifier}
 * </em>
 */
public class ASTInterfaceModifierList extends ASTListNode<ASTKeywordNode> {
    /**
     * Constructs an <code>ASTInterfaceModifierList</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTKeywordNode</code>s.
     */
    public ASTInterfaceModifierList(Location location, List<ASTKeywordNode> children) {
        super(location, children, Type.INTERFACE_MODIFIERS);
    }
}
